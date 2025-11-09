package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.*;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@PrimaryPort
public interface EventCertificateService {

    Optional<EventCertificate> findById(EventCertificateId eventCertificateCertificateCertificateId);

    List<EventCertificate> findAll();

    @NonNull
    EventCertificate updateEventCertificate(
            EventCertificateId id,
            EventCertificateName name,
            EventId event,
            EventCertificateLayoutDescription eventCertificateLayoutDescription,
            MediaFileId mediaFile,
            boolean primary);

    @Transactional
    void deleteEventCertificate(EventCertificateId eventCertificateCertificateId);

    @NonNull
    EventCertificate createEventCertificate(
            String eventCertificateCertificateName,
            EventId event,
            String eventCertificateLayoutDescription,
            MediaFileId mediaFile,
            boolean primary);

    Page<EventCertificate> findAll(@Nullable String filter, @NonNull Pageable pageable);

    EventCertificate getById(EventCertificateId id);
}
