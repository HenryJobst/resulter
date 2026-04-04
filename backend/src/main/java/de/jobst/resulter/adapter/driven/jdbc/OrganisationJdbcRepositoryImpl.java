package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.OrganisationType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Custom implementation of OrganisationJdbcRepositoryCustom using JdbcClient.
 * This implementation avoids N+1 queries by not loading MappedCollections during pagination.
 */
public class OrganisationJdbcRepositoryImpl implements OrganisationJdbcRepositoryCustom {

    private final JdbcClient jdbcClient;

    public OrganisationJdbcRepositoryImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<OrganisationDbo> findAllOrganisationsWithoutChildOrganisations() {
        String query = "SELECT id, name, short_name, type, country_id " + "FROM organisation " + "ORDER BY id";

        return jdbcClient.sql(query).query(new OrganisationDboRowMapper()).list();
    }

    @Override
    public List<OrganisationDbo> findAllByIdWithoutChildOrganisations(Collection<Long> organisationIds) {
        if (organisationIds == null || organisationIds.isEmpty()) {
            return Collections.emptyList();
        }

        String query = "SELECT id, name, short_name, type, country_id " + "FROM organisation "
                + "WHERE id IN (:ids) "
                + "ORDER BY id";

        return jdbcClient
                .sql(query)
                .param("ids", organisationIds)
                .query(new OrganisationDboRowMapper())
                .list();
    }

    @Override
    public Page<OrganisationDbo> findAllWithoutChildOrganisations(Pageable pageable) {
        return findAllWithoutChildOrganisations(
                null,
                ExampleMatcher.StringMatcher.CONTAINING,
                null,
                ExampleMatcher.StringMatcher.CONTAINING,
                null,
                pageable);
    }

    @Override
    public Page<OrganisationDbo> findAllWithoutChildOrganisations(
            @Nullable String nameFilter,
            ExampleMatcher.StringMatcher nameMatcher,
            @Nullable String shortNameFilter,
            ExampleMatcher.StringMatcher shortNameMatcher,
            @Nullable Long idFilter,
            Pageable pageable) {
        // Build WHERE clause
        WhereClauseResult whereClause =
                buildWhereClause(nameFilter, nameMatcher, shortNameFilter, shortNameMatcher, idFilter);

        // Build ORDER BY clause from Pageable sort
        String orderByClause = buildOrderByClause(pageable.getSort());

        // Build main query with pagination
        String query = "SELECT id, name, short_name, type, country_id " + "FROM organisation "
                + whereClause.clause()
                + " "
                + orderByClause
                + " LIMIT ? OFFSET ?";

        // Build count query
        String countQuery = "SELECT COUNT(*) FROM organisation " + whereClause.clause();

        // Execute queries
        JdbcClient.StatementSpec querySpec = jdbcClient.sql(query);
        JdbcClient.StatementSpec countSpec = jdbcClient.sql(countQuery);

        // Add filter parameters if present
        for (int i = 0; i < whereClause.params().size(); i++) {
            querySpec = querySpec.param(whereClause.params().get(i));
            countSpec = countSpec.param(whereClause.params().get(i));
        }

        // Execute paginated query
        List<OrganisationDbo> organisationDbos = querySpec
                .param(pageable.getPageSize())
                .param(pageable.getOffset())
                .query(new OrganisationDboRowMapper())
                .list();

        // Get total count for pagination
        Long total = countSpec.query(Long.class).single();

        return new PageImpl<>(organisationDbos, pageable, total != null ? total : 0L);
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
            @Nullable String shortNameFilter,
            ExampleMatcher.StringMatcher shortNameMatcher,
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

        // Short name filter
        if (shortNameFilter != null && !shortNameFilter.isBlank()) {
            if (shortNameMatcher == ExampleMatcher.StringMatcher.EXACT) {
                conditions.add("LOWER(short_name) = ?");
            } else {
                conditions.add("LOWER(short_name) LIKE ?");
            }
            params.add(buildLikePattern(shortNameFilter, shortNameMatcher));
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
        String dboProperty = OrganisationDbo.mapOrdersDomainToDbo(order);

        // Then map from DBO property names to database column names
        return switch (dboProperty) {
            case "id" -> "id";
            case "name" -> "name";
            case "shortName" -> "short_name";
            case "type" -> "type";
            case "country.name" -> "country_id";
            case "childOrganisations" -> "id";
            default -> "id";
        };
    }

    /**
     * RowMapper for OrganisationDbo that does not load the MappedCollection.
     */
    private static class OrganisationDboRowMapper implements RowMapper<OrganisationDbo> {
        @Override
        public OrganisationDbo mapRow(ResultSet rs, int rowNum) throws SQLException {
            OrganisationDbo organisationDbo = new OrganisationDbo();

            // Use @With annotation for id (creates new instance with id set)
            Long id = rs.getLong("id");
            if (!rs.wasNull()) {
                organisationDbo = organisationDbo.withId(id);
            }

            // Set remaining fields using setters
            organisationDbo.setName(rs.getString("name"));
            organisationDbo.setShortName(rs.getString("short_name"));

            String typeStr = rs.getString("type");
            organisationDbo.setType(typeStr != null ? OrganisationType.valueOf(typeStr) : null);

            Long countryId = rs.getLong("country_id");
            if (!rs.wasNull()) {
                organisationDbo.setCountry(AggregateReference.to(countryId));
            } else {
                organisationDbo.setCountry(null);
            }

            // childOrganisations will be populated later by batch loading
            organisationDbo.setChildOrganisations(new HashSet<>());

            return organisationDbo;
        }
    }
}
