package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.Discipline;
import de.jobst.resulter.domain.EventStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Custom implementation of EventJdbcRepositoryCustom using JdbcClient.
 * This implementation avoids N+1 queries by not loading MappedCollections during pagination.
 */
public class EventJdbcRepositoryImpl implements EventJdbcRepositoryCustom {

    private final JdbcClient jdbcClient;

    public EventJdbcRepositoryImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<EventDbo> findAllEventsWithoutOrganisations() {
        return jdbcClient
                .sql("SELECT id, name, start_time, start_time_zone, end_time, end_time_zone, "
                        + "state, discipline, aggregate_score FROM event ORDER BY id")
                .query(new EventDboRowMapper())
                .list();
    }

    @Override
    public List<EventDbo> findAllByIdWithoutOrganisations(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }
        return jdbcClient
                .sql("SELECT id, name, start_time, start_time_zone, end_time, end_time_zone, "
                        + "state, discipline, aggregate_score FROM event WHERE id IN (:ids) ORDER BY id")
                .param("ids", eventIds)
                .query(new EventDboRowMapper())
                .list();
    }

    @Override
    public Page<EventDbo> findAllWithoutOrganisations(Pageable pageable) {
        return findAllWithoutOrganisations(null, ExampleMatcher.StringMatcher.CONTAINING, pageable);
    }

    @Override
    public Page<EventDbo> findAllWithoutOrganisations(
            @Nullable String nameFilter, ExampleMatcher.StringMatcher nameMatcher, Pageable pageable) {
        // Build WHERE clause
        String whereClause = buildWhereClause(nameFilter, nameMatcher);

        // Build ORDER BY clause from Pageable sort
        String orderByClause = buildOrderByClause(pageable.getSort());

        // Build main query with pagination
        String query =
                "SELECT id, name, start_time, start_time_zone, end_time, end_time_zone, state, discipline, aggregate_score "
                        + "FROM event " + whereClause + " " + orderByClause + " LIMIT ? OFFSET ?";

        // Build count query
        String countQuery = "SELECT COUNT(*) FROM event " + whereClause;

        // Execute queries
        JdbcClient.StatementSpec querySpec = jdbcClient.sql(query);
        JdbcClient.StatementSpec countSpec = jdbcClient.sql(countQuery);

        // Add filter parameter if present
        if (nameFilter != null && !nameFilter.isBlank()) {
            String pattern = buildLikePattern(nameFilter, nameMatcher);
            querySpec = querySpec.param(pattern);
            countSpec = countSpec.param(pattern);
        }

        // Execute paginated query
        List<EventDbo> eventDbos = querySpec
                .param(pageable.getPageSize())
                .param(pageable.getOffset())
                .query(new EventDboRowMapper())
                .list();

        // Get total count for pagination
        Long total = countSpec.query(Long.class).single();

        return new PageImpl<>(eventDbos, pageable, total != null ? total : 0L);
    }

    /**
     * Builds WHERE clause for name filtering.
     */
    private String buildWhereClause(@Nullable String nameFilter, ExampleMatcher.StringMatcher nameMatcher) {
        if (nameFilter == null || nameFilter.isBlank()) {
            return "";
        }

        // For EXACT matching, use = operator, otherwise use LIKE
        if (nameMatcher == ExampleMatcher.StringMatcher.EXACT) {
            return "WHERE LOWER(name) = ?";
        } else {
            return "WHERE LOWER(name) LIKE ?";
        }
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
        String dboProperty = EventDbo.mapOrdersDomainToDbo(order);

        // Then map from DBO property names to database column names
        return switch (dboProperty) {
            case "id" -> "id";
            case "name" -> "name";
            case "startTime" -> "start_time";
            case "endTime" -> "end_time";
            case "state" -> "state";
            case "discipline" -> "discipline";
            case "aggregateScore" -> "aggregate_score";
            default -> "id";
        };
    }

    /**
     * RowMapper for EventDbo that does not load the MappedCollection.
     */
    private static class EventDboRowMapper implements RowMapper<EventDbo> {
        @Override
        public EventDbo mapRow(ResultSet rs, int rowNum) throws SQLException {
            EventDbo eventDbo = new EventDbo();

            // Use @With annotation for id (creates new instance with id set)
            Long id = rs.getLong("id");
            if (!rs.wasNull()) {
                eventDbo = eventDbo.withId(id);
            }

            // Set remaining fields using setters
            eventDbo.setName(rs.getString("name"));

            Timestamp startTime = rs.getTimestamp("start_time");
            eventDbo.setStartTime(startTime);
            eventDbo.setStartTimeZone(rs.getString("start_time_zone"));

            Timestamp endTime = rs.getTimestamp("end_time");
            eventDbo.setEndTime(endTime);
            eventDbo.setEndTimeZone(rs.getString("end_time_zone"));

            String stateStr = rs.getString("state");
            eventDbo.setState(stateStr != null ? EventStatus.valueOf(stateStr) : null);

            String disciplineStr = rs.getString("discipline");
            eventDbo.setDiscipline(disciplineStr != null ? Discipline.valueOf(disciplineStr) : Discipline.getDefault());

            eventDbo.setAggregateScore(rs.getBoolean("aggregate_score"));

            // organisations will be populated later by batch loading
            eventDbo.setOrganisations(new HashSet<>());

            return eventDbo;
        }
    }
}
