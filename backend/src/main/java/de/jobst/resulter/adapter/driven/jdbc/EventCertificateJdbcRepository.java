package de.jobst.resulter.adapter.driven.jdbc;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EventCertificateJdbcRepository
        extends CrudRepository<EventCertificateDbo, Long>,
                PagingAndSortingRepository<EventCertificateDbo, Long>,
                QueryByExampleExecutor<EventCertificateDbo> {

    Collection<EventCertificateDbo> findAll();

    Optional<EventCertificateDbo> findByEventAndPrimary(AggregateReference<EventDbo, Long> event, boolean primary);

    Collection<EventCertificateDbo> findAllByEvent(AggregateReference<EventDbo, Long> event);
}
