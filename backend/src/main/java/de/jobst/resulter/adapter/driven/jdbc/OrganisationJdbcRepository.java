package de.jobst.resulter.adapter.driven.jdbc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganisationJdbcRepository
        extends CrudRepository<OrganisationDbo, Long>,
                PagingAndSortingRepository<OrganisationDbo, Long>,
                QueryByExampleExecutor<OrganisationDbo>,
                OrganisationJdbcRepositoryCustom {

    Collection<OrganisationDbo> findAll();

    Optional<OrganisationDbo> findByName(String name);

    /**
     * Find all organisations without loading the MappedCollection (childOrganisations).
     * Use this for batch loading to avoid N+1 queries.
     */
    @Query("SELECT * FROM organisation ORDER BY id")
    List<OrganisationDbo> findAllOrganisationsWithoutChildOrganisations();

    /**
     * Find organisations by IDs without loading the MappedCollection (childOrganisations).
     * Use this for batch loading to avoid N+1 queries.
     */
    @Query("SELECT * FROM organisation WHERE id IN (:organisationIds) ORDER BY id")
    List<OrganisationDbo> findAllByIdWithoutChildOrganisations(
            @Param("organisationIds") Collection<Long> organisationIds);

    /**
     * Find all organisation-organisation mappings for given organisation IDs.
     * Use this for batch loading child organisations after loading organisations.
     */
    @Query("SELECT * FROM organisation_organisation WHERE parent_organisation_id IN (:organisationIds)")
    List<OrganisationOrganisationDbo> findChildOrganisationsByOrganisationIds(
            @Param("organisationIds") Collection<Long> organisationIds);
}
