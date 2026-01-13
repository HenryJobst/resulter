package de.jobst.resulter.adapter.driven.jdbc;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT * FROM event_certificate WHERE event_id IN (:eventIds) AND \"primary\" = true")
    Collection<EventCertificateDbo> findPrimaryByEventIdIn(@Param("eventIds") Set<Long> eventIds);
}
