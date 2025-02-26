package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.EventCertificateStat;
import de.jobst.resulter.domain.EventCertificateStatId;
import de.jobst.resulter.domain.EventId;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

@Repository
@SecondaryPort
public interface EventCertificateStatRepository {

    EventCertificateStat save(EventCertificateStat eventCertificateStat);

    void delete(EventCertificateStat eventCertificateStat);

    List<EventCertificateStat> findAll();

    Optional<EventCertificateStat> findById(EventCertificateStatId id);

    Page<EventCertificateStat> findAll(@Nullable String filter, @NonNull Pageable pageable);

    List<EventCertificateStat> findAllByEvent(EventId id);

    void saveAll(List<EventCertificateStat> eventCertificateStats);

    void deleteAllByEventId(EventId eventId);

    void deleteById(EventCertificateStatId id);
}
