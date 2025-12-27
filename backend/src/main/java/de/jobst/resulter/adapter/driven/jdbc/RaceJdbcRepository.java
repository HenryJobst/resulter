package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RaceJdbcRepository extends CrudRepository<RaceDbo, Long>, PagingAndSortingRepository<RaceDbo, Long> {

    Collection<RaceDbo> findAll();

    Optional<RaceDbo> findByEventIdAndNumber(AggregateReference<EventDbo, Long> eventId, Byte number);

    List<RaceDbo> findAllByEventIdIn(Collection<AggregateReference<EventDbo, Long>> events);
}
