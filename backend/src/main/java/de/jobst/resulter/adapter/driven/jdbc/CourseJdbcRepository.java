package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface CourseJdbcRepository
    extends CrudRepository<CourseDbo, Long>, PagingAndSortingRepository<CourseDbo, Long> {

    Collection<CourseDbo> findAll();

    Optional<CourseDbo> findByEventIdAndName(AggregateReference<EventDbo, Long> eventId, String name);

    Collection<CourseDbo> findAllByEventIdAndNameIn(AggregateReference<EventDbo, Long> eventId,
                                                    Collection<String> names);
}
