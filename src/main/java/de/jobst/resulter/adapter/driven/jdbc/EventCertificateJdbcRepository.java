package de.jobst.resulter.adapter.driven.jdbc;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface EventCertificateJdbcRepository
    extends CrudRepository<EventCertificateDbo, Long>, PagingAndSortingRepository<EventCertificateDbo, Long> {

    @NonNull
    Collection<EventCertificateDbo> findAll();
}
