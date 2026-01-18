package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.CupType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Custom implementation of CupJdbcRepositoryCustom using JdbcClient.
 * This implementation avoids N+1 queries by not loading MappedCollections during pagination.
 */
public class CupJdbcRepositoryImpl implements CupJdbcRepositoryCustom {

    private final JdbcClient jdbcClient;

    public CupJdbcRepositoryImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Page<CupDbo> findAllWithoutEvents(Pageable pageable) {
        return findAllWithoutEvents(null, ExampleMatcher.StringMatcher.CONTAINING, null, null, pageable);
    }

    @Override
    public Page<CupDbo> findAllWithoutEvents(
            @Nullable String nameFilter,
            ExampleMatcher.StringMatcher nameMatcher,
            @Nullable Integer yearFilter,
            @Nullable Long idFilter,
            Pageable pageable) {
        // Build WHERE clause
        WhereClauseResult whereClause = buildWhereClause(nameFilter, nameMatcher, yearFilter, idFilter);

        // Build ORDER BY clause from Pageable sort
        String orderByClause = buildOrderByClause(pageable.getSort());

        // Build main query with pagination
        String query = "SELECT id, name, type, year " + "FROM cup "
                + whereClause.clause()
                + " "
                + orderByClause
                + " LIMIT ? OFFSET ?";

        // Build count query
        String countQuery = "SELECT COUNT(*) FROM cup " + whereClause.clause();

        // Execute queries
        JdbcClient.StatementSpec querySpec = jdbcClient.sql(query);
        JdbcClient.StatementSpec countSpec = jdbcClient.sql(countQuery);

        // Add filter parameters if present
        for (int i = 0; i < whereClause.params().size(); i++) {
            querySpec = querySpec.param(whereClause.params().get(i));
            countSpec = countSpec.param(whereClause.params().get(i));
        }

        // Execute paginated query
        List<CupDbo> cupDbos = querySpec
                .param(pageable.getPageSize())
                .param(pageable.getOffset())
                .query(new CupDboRowMapper())
                .list();

        // Get total count for pagination
        Long total = countSpec.query(Long.class).single();

        return new PageImpl<>(cupDbos, pageable, total != null ? total : 0L);
    }

    /**
     * Result of WHERE clause building with clause string and parameters.
     */
    private record WhereClauseResult(String clause, List<Object> params) {}

    /**
     * Builds WHERE clause for filtering.
     */
    private WhereClauseResult buildWhereClause(
            @Nullable String nameFilter,
            ExampleMatcher.StringMatcher nameMatcher,
            @Nullable Integer yearFilter,
            @Nullable Long idFilter) {
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        // Name filter
        if (nameFilter != null && !nameFilter.isBlank()) {
            if (nameMatcher == ExampleMatcher.StringMatcher.EXACT) {
                conditions.add("LOWER(name) = ?");
            } else {
                conditions.add("LOWER(name) LIKE ?");
            }
            params.add(buildLikePattern(nameFilter, nameMatcher));
        }

        // Year filter
        if (yearFilter != null) {
            conditions.add("year = ?");
            params.add(yearFilter);
        }

        // ID filter
        if (idFilter != null) {
            conditions.add("id = ?");
            params.add(idFilter);
        }

        if (conditions.isEmpty()) {
            return new WhereClauseResult("", params);
        }

        return new WhereClauseResult("WHERE " + String.join(" AND ", conditions), params);
    }

    /**
     * Builds LIKE pattern based on matcher type.
     */
    private String buildLikePattern(String value, ExampleMatcher.StringMatcher matcher) {
        String valueLower = value.toLowerCase();
        return switch (matcher) {
            case STARTING -> valueLower + "%";
            case ENDING -> "%" + valueLower;
            case EXACT -> valueLower;
            default -> "%" + valueLower + "%"; // CONTAINING and default
        };
    }

    /**
     * Builds ORDER BY clause from Spring Data Sort object.
     */
    private String buildOrderByClause(Sort sort) {
        if (sort.isUnsorted()) {
            return "ORDER BY id";
        }

        StringBuilder orderBy = new StringBuilder("ORDER BY ");
        sort.forEach(order -> {
            String mappedProperty = mapSortProperty(order.getProperty());
            orderBy.append(mappedProperty)
                    .append(" ")
                    .append(order.getDirection().name())
                    .append(", ");
        });

        // Remove trailing comma and space
        orderBy.setLength(orderBy.length() - 2);
        return orderBy.toString();
    }

    /**
     * Maps domain property names to database column names.
     */
    private String mapSortProperty(String property) {
        // First map from domain to DBO property names
        Sort.Order order = Sort.Order.by(property);
        String dboProperty = CupDbo.mapOrdersDomainToDbo(order);

        // Then map from DBO property names to database column names
        return switch (dboProperty) {
            case "id" -> "id";
            case "name" -> "name";
            case "type" -> "type";
            case "year" -> "year";
            default -> dboProperty;
        };
    }

    /**
     * RowMapper for CupDbo that does not load the MappedCollection.
     */
    private static class CupDboRowMapper implements RowMapper<CupDbo> {
        @Override
        public CupDbo mapRow(ResultSet rs, int rowNum) throws SQLException {
            CupDbo cupDbo = new CupDbo();

            // Use @With annotation for id (creates new instance with id set)
            Long id = rs.getLong("id");
            if (!rs.wasNull()) {
                cupDbo = cupDbo.withId(id);
            }

            // Set remaining fields using setters
            cupDbo.setName(rs.getString("name"));

            String typeStr = rs.getString("type");
            cupDbo.setType(typeStr != null ? CupType.valueOf(typeStr) : null);

            Integer year = rs.getInt("year");
            if (!rs.wasNull()) {
                cupDbo.setYear(year);
            }

            // events will be populated later by batch loading
            cupDbo.setEvents(new HashSet<>());

            return cupDbo;
        }
    }
}
