package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface EventCertificateStatJdbcRepository
    extends CrudRepository<EventCertificateStatDbo, Long>, PagingAndSortingRepository<EventCertificateStatDbo, Long> {

    @NonNull
    Collection<EventCertificateStatDbo> findAll();

    Collection<EventCertificateStatDbo> findAllByEvent(AggregateReference<EventDbo, Long> event);
}
