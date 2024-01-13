package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface EventJdbcRepository
    extends CrudRepository<EventDbo, Long>, PagingAndSortingRepository<EventDbo, Long> {

    @NonNull
    Collection<EventDbo> findAll();

    Optional<EventDbo> findByName(String name);
}
