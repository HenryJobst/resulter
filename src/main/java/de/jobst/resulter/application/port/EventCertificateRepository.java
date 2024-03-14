package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.EventId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

public interface EventCertificateRepository {

    EventCertificate save(EventCertificate event);

    void delete(EventCertificate event);

    List<EventCertificate> findAll();

    Optional<EventCertificate> findById(EventCertificateId id);

    Page<EventCertificate> findAll(@Nullable String filter, @NonNull Pageable pageable);

    List<EventCertificate> findAllByEvent(EventId id);

    void saveAll(List<EventCertificate> eventCertificates);
}
