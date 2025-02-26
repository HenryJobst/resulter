package de.jobst.resulter.application.port;

import de.jobst.resulter.adapter.driver.web.dto.EventKeyDto;
import de.jobst.resulter.adapter.driver.web.dto.MediaFileKeyDto;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.EventCertificateLayoutDescription;
import de.jobst.resulter.domain.EventCertificateName;
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
            EventKeyDto event,
            EventCertificateLayoutDescription eventCertificateLayoutDescription,
            MediaFileKeyDto mediaFile,
            boolean primary);

    @Transactional
    void deleteEventCertificate(EventCertificateId eventCertificateCertificateId);

    @NonNull
    EventCertificate createEventCertificate(
            String eventCertificateCertificateName,
            EventKeyDto event,
            String eventCertificateLayoutDescription,
            MediaFileKeyDto mediaFile,
            boolean primary);

    Page<EventCertificate> findAll(@Nullable String filter, @NonNull Pageable pageable);

    EventCertificate getById(EventCertificateId id);
}
