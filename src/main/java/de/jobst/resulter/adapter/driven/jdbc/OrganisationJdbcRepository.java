package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface OrganisationJdbcRepository
    extends CrudRepository<OrganisationDbo, Long>, PagingAndSortingRepository<OrganisationDbo, Long> {

    //Collection<OrganisationDbo> findAllById()
    @NonNull
    Collection<OrganisationDbo> findAll();

    Optional<OrganisationDbo> findByName(String name);
}
