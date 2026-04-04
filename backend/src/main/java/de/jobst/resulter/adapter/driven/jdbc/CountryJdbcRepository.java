package de.jobst.resulter.adapter.driven.jdbc;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryJdbcRepository
        extends CrudRepository<CountryDbo, Long>, PagingAndSortingRepository<CountryDbo, Long> {

    Collection<CountryDbo> findAll();

    Optional<CountryDbo> findByCode(String code);

    @Query("SELECT * FROM country WHERE id IN (:ids)")
    Collection<CountryDbo> findAllByIdIn(@Param("ids") Set<Long> ids);
}
