package de.jobst.resulter.adapter.driven.jdbc;

import java.util.Collection;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom repository fragment for optimized Organisation queries.
 * Provides methods that avoid N+1 queries by not loading MappedCollections.
 */
public interface OrganisationJdbcRepositoryCustom {

    /**
     * Find all organisations without loading the MappedCollection (childOrganisations).
     * Uses JdbcClient with custom RowMapper to avoid Spring Data JDBC auto-loading.
     * Use this for batch loading to avoid N+1 queries.
     *
     * @return List of all OrganisationDbo objects without loaded childOrganisations
     */
    List<OrganisationDbo> findAllOrganisationsWithoutChildOrganisations();

    /**
     * Find organisations by IDs without loading the MappedCollection (childOrganisations).
     * Uses JdbcClient with custom RowMapper to avoid Spring Data JDBC auto-loading.
     * Use this for batch loading to avoid N+1 queries.
     *
     * @param organisationIds Collection of organisation IDs to load
     * @return List of OrganisationDbo objects without loaded childOrganisations
     */
    List<OrganisationDbo> findAllByIdWithoutChildOrganisations(Collection<Long> organisationIds);

    /**
     * Find all organisations with pagination, without loading the MappedCollection (childOrganisations).
     * Use this for batch loading to avoid N+1 queries.
     *
     * @param pageable Pagination and sorting information
     * @return Page of OrganisationDbo objects without loaded childOrganisations
     */
    Page<OrganisationDbo> findAllWithoutChildOrganisations(Pageable pageable);

    /**
     * Find all organisations with pagination and optional filters, without loading the MappedCollection.
     * Use this for batch loading to avoid N+1 queries.
     *
     * @param nameFilter Optional name filter value (can be null)
     * @param nameMatcher String matcher type for name filtering
     * @param shortNameFilter Optional short name filter value (can be null)
     * @param shortNameMatcher String matcher type for short name filtering
     * @param idFilter Optional id filter value (can be null)
     * @param pageable Pagination and sorting information
     * @return Page of OrganisationDbo objects without loaded childOrganisations
     */
    Page<OrganisationDbo> findAllWithoutChildOrganisations(
            @Nullable String nameFilter,
            ExampleMatcher.StringMatcher nameMatcher,
            @Nullable String shortNameFilter,
            ExampleMatcher.StringMatcher shortNameMatcher,
            @Nullable Long idFilter,
            Pageable pageable);
}
