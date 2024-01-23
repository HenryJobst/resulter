package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
@RepositoryRestResource(path = "split_time_lists")
public interface SplitTimeListJdbcRepository
    extends CrudRepository<SplitTimeListDbo, Long>, PagingAndSortingRepository<SplitTimeListDbo, Long> {

    @NonNull
    Collection<SplitTimeListDbo> findAll();

    Optional<SplitTimeListDbo> findByEventAndResultListAndClassResultShortNameAndPersonAndRaceNumber(AggregateReference<EventDbo, Long> event,
                                                                                                     AggregateReference<ResultListDbo, Long> resultList,
                                                                                                     String classResultShortName,
                                                                                                     AggregateReference<PersonDbo, Long> person,
                                                                                                     Long raceNumber);
}