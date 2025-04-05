package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface CountryJdbcRepository
    extends CrudRepository<CountryDbo, Long>, PagingAndSortingRepository<CountryDbo, Long> {

    @NonNull
    Collection<CountryDbo> findAll();

    Optional<CountryDbo> findByCode(String code);
}
