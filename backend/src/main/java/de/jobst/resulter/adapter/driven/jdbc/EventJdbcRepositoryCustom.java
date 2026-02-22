package de.jobst.resulter.adapter.driven.jdbc;

import java.util.Collection;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom repository fragment for optimized Event queries.
 * Provides methods that avoid N+1 queries by not loading MappedCollections.
 */
public interface EventJdbcRepositoryCustom {

    /**
     * Find all events without loading the MappedCollection (organisations).
     * Uses JdbcClient with custom RowMapper to avoid Spring Data JDBC auto-loading.
     */
    List<EventDbo> findAllEventsWithoutOrganisations();

    /**
     * Find events by IDs without loading the MappedCollection (organisations).
     * Uses JdbcClient with custom RowMapper to avoid Spring Data JDBC auto-loading.
     */
    List<EventDbo> findAllByIdWithoutOrganisations(Collection<Long> eventIds);


    /**
     * Find all events with pagination, without loading the MappedCollection (organisations).
     * Use this for batch loading to avoid N+1 queries.
     *
     * @param pageable Pagination and sorting information
     * @return Page of EventDbo objects without loaded organisations
     */
    Page<EventDbo> findAllWithoutOrganisations(Pageable pageable);

    /**
     * Find all events with pagination and optional name filter, without loading the MappedCollection.
     * Use this for batch loading to avoid N+1 queries.
     *
     * @param nameFilter Optional name filter value (can be null)
     * @param nameMatcher String matcher type for name filtering
     * @param pageable Pagination and sorting information
     * @return Page of EventDbo objects without loaded organisations
     */
    Page<EventDbo> findAllWithoutOrganisations(
            @Nullable String nameFilter, ExampleMatcher.StringMatcher nameMatcher, Pageable pageable);
}
