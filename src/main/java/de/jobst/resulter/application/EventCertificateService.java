package de.jobst.resulter.application;

import de.jobst.resulter.adapter.driver.web.dto.EventKeyDto;
import de.jobst.resulter.adapter.driver.web.dto.MediaFileKeyDto;
import de.jobst.resulter.application.port.*;
import de.jobst.resulter.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EventCertificateService {

    public final PersonRepository personRepository;
    public final OrganisationRepository organisationRepository;
    private final EventCertificateRepository eventCertificateRepository;

    private final EventRepository eventRepository;
    private final MediaFileRepository mediaFileRepository;

    public EventCertificateService(EventCertificateRepository eventCertificateRepository,
                                   PersonRepository personRepository,
                                   OrganisationRepository organisationRepository,
                                   EventRepository eventRepository,
                                   MediaFileRepository mediaFileRepository) {
        this.eventCertificateRepository = eventCertificateRepository;
        this.personRepository = personRepository;
        this.organisationRepository = organisationRepository;
        this.eventRepository = eventRepository;
        this.mediaFileRepository = mediaFileRepository;
    }

    public Optional<EventCertificate> findById(EventCertificateId eventCertificateCertificateCertificateId) {
        return eventCertificateRepository.findById(eventCertificateCertificateCertificateId);
    }

    public List<EventCertificate> findAll() {
        return eventCertificateRepository.findAll();
    }

    public EventCertificate updateEventCertificate(EventCertificateId id,
                                                   EventCertificateName name,
                                                   EventKeyDto event,
                                                   EventCertificateLayoutDescription eventCertificateLayoutDescription,
                                                   MediaFileKeyDto mediaFile) {
        Optional<EventCertificate> optionalEventCertificate = findById(id);
        if (optionalEventCertificate.isEmpty()) {
            return null;
        }
        Optional<Event> optionalEvent = eventRepository.findById(EventId.of(event.id()));
        if (optionalEvent.isEmpty()) {
            return null;
        }
        Optional<MediaFile> optionalMediaFile = mediaFileRepository.findById(MediaFileId.of(mediaFile.id()));
        if (optionalMediaFile.isEmpty()) {
            return null;
        }
        EventCertificate eventCertificateCertificate = optionalEventCertificate.get();
        eventCertificateCertificate.update(name,
            optionalEvent.get(),
            eventCertificateLayoutDescription,
            optionalMediaFile.get());
        return eventCertificateRepository.save(eventCertificateCertificate);
    }

    @Transactional
    public boolean deleteEventCertificate(EventCertificateId eventCertificateCertificateId) {
        Optional<EventCertificate> optionalEventCertificate = findById(eventCertificateCertificateId);
        if (optionalEventCertificate.isEmpty()) {
            return false;
        }
        eventCertificateRepository.delete(optionalEventCertificate.get());
        return true;
    }


    public EventCertificate createEventCertificate(String eventCertificateCertificateName,
                                                   EventKeyDto event,
                                                   String eventCertificateLayoutDescription,
                                                   MediaFileKeyDto mediaFile) {

        Optional<Event> optionalEvent =
            event != null ? eventRepository.findById(EventId.of(event.id())) : Optional.empty();
        if (optionalEvent.isEmpty()) {
            return null;
        }
        Optional<MediaFile> optionalMediaFile =
            mediaFile != null ? mediaFileRepository.findById(MediaFileId.of(mediaFile.id())) : Optional.empty();
        if (optionalMediaFile.isEmpty()) {
            return null;
        }
        EventCertificate eventCertificateCertificate = EventCertificate.of(EventCertificateId.empty().value(),
            eventCertificateCertificateName,
            optionalEvent.get(),
            eventCertificateLayoutDescription,
            optionalMediaFile.get());
        return eventCertificateRepository.save(eventCertificateCertificate);
    }

    public Page<EventCertificate> findAll(@Nullable String filter, @NonNull Pageable pageable) {
        return eventCertificateRepository.findAll(filter, pageable);
    }
}
