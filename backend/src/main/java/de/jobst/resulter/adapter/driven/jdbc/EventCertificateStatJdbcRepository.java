package de.jobst.resulter.adapter.driven.jdbc;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface EventCertificateStatJdbcRepository
    extends CrudRepository<EventCertificateStatDbo, Long>, PagingAndSortingRepository<EventCertificateStatDbo, Long> {

    @NonNull
    Collection<EventCertificateStatDbo> findAll();

    Collection<EventCertificateStatDbo> findAllByEvent(AggregateReference<EventDbo, Long> event);

    @Modifying
    @Query("""
        UPDATE event_certificate_stat
        SET person_id = :newPersonId
        WHERE person_id = :oldPersonId;
        """)
    long replacePersonId(@Param("oldPersonId") Long oldPersonId,
                         @Param("newPersonId") Long newPersonId);
}
