package de.jobst.resulter.adapter.driven.jdbc;

import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface EventJdbcRepository extends CrudRepository<EventDbo, Long>,
                                             PagingAndSortingRepository<EventDbo, Long>,
                                             QueryByExampleExecutor<EventDbo> {

    @NonNull
    Collection<EventDbo> findAll();

    Optional<EventDbo> findByName(String name);
}
