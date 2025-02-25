package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.EventId;
import java.util.List;
import java.util.Optional;
import org.jmolecules.ddd.annotation.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Repository
public interface EventCertificateRepository {

    @NonNull
    EventCertificate save(@NonNull EventCertificate event);

    void delete(EventCertificate event);

    List<EventCertificate> findAll();

    Optional<EventCertificate> findById(EventCertificateId id);

    Page<EventCertificate> findAll(@Nullable String filter, @NonNull Pageable pageable);

    List<EventCertificate> findAllByEvent(EventId id);

    void saveAll(List<EventCertificate> eventCertificates);

    void deleteAllByEventId(EventId eventId);
}
