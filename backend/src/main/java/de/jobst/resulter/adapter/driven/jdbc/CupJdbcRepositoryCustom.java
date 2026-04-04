package de.jobst.resulter.adapter.driven.jdbc;

import java.util.Collection;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom repository fragment for optimized Cup queries.
 * Provides methods that avoid N+1 queries by not loading MappedCollections.
 */
public interface CupJdbcRepositoryCustom {

    /**
     * Find all cups without loading the MappedCollection (events).
     * Uses JdbcClient with custom RowMapper to avoid Spring Data JDBC auto-loading.
     */
    List<CupDbo> findAllCupsWithoutEvents();

    /**
     * Find cups by IDs without loading the MappedCollection (events).
     * Uses JdbcClient with custom RowMapper to avoid Spring Data JDBC auto-loading.
     */
    List<CupDbo> findAllByIdWithoutEvents(Collection<Long> cupIds);

    /**
     * Find cups for a specific event without loading the MappedCollection (events).
     * Uses JdbcClient with custom RowMapper to avoid Spring Data JDBC auto-loading.
     */
    List<CupDbo> findByEventIdWithoutEvents(Long eventId);


    /**
     * Find all cups with pagination, without loading the MappedCollection (events).
     * Use this for batch loading to avoid N+1 queries.
     *
     * @param pageable Pagination and sorting information
     * @return Page of CupDbo objects without loaded events
     */
    Page<CupDbo> findAllWithoutEvents(Pageable pageable);

    /**
     * Find all cups with pagination and optional filters, without loading the MappedCollection.
     * Use this for batch loading to avoid N+1 queries.
     *
     * @param nameFilter Optional name filter value (can be null)
     * @param nameMatcher String matcher type for name filtering
     * @param yearFilter Optional year filter value (can be null)
     * @param idFilter Optional id filter value (can be null)
     * @param pageable Pagination and sorting information
     * @return Page of CupDbo objects without loaded events
     */
    Page<CupDbo> findAllWithoutEvents(
            @Nullable String nameFilter,
            ExampleMatcher.StringMatcher nameMatcher,
            @Nullable Integer yearFilter,
            @Nullable Long idFilter,
            Pageable pageable);
}
