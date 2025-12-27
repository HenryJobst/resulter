package de.jobst.resulter.adapter.driven.jdbc;

import java.util.Collection;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganisationJdbcRepository
        extends CrudRepository<OrganisationDbo, Long>,
                PagingAndSortingRepository<OrganisationDbo, Long>,
                QueryByExampleExecutor<OrganisationDbo> {

    // Collection<OrganisationDbo> findAllById()
    @NonNull
    Collection<OrganisationDbo> findAll();

    Optional<OrganisationDbo> findByName(String name);
}
