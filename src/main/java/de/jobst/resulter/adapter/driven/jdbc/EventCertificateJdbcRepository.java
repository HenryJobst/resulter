package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface EventCertificateJdbcRepository
    extends CrudRepository<EventCertificateDbo, Long>, PagingAndSortingRepository<EventCertificateDbo, Long> {

    @NonNull
    Collection<EventCertificateDbo> findAll();

    Optional<EventCertificateDbo> findByEventAndPrimary(AggregateReference<EventDbo, Long> event, boolean primary);

    Collection<EventCertificateDbo> findAllByEvent(AggregateReference<EventDbo, Long> event);
}
