package de.jobst.resulter.application;

import de.jobst.resulter.adapter.driver.web.dto.EventKeyDto;
import de.jobst.resulter.adapter.driver.web.dto.MediaFileKeyDto;
import de.jobst.resulter.application.port.*;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
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

    public @NonNull EventCertificate updateEventCertificate(EventCertificateId id,
                                                   EventCertificateName name,
                                                   EventKeyDto event,
                                                   EventCertificateLayoutDescription eventCertificateLayoutDescription,
                                                   MediaFileKeyDto mediaFile,
                                                   boolean primary) {
        EventCertificate eventCertificate = findById(id).orElseThrow(ResourceNotFoundException::new);
        Optional<Event> optionalEvent = event != null ? eventRepository.findById(EventId.of(event.id())) :
                                        Optional.empty();

        Optional<MediaFile> optionalMediaFile =
            mediaFile != null ? mediaFileRepository.findById(MediaFileId.of(mediaFile.id())) : Optional.empty();

        eventCertificate.update(name,
            optionalEvent.orElse(null),
            eventCertificateLayoutDescription,
            optionalMediaFile.orElse(null),
            primary);

        if (optionalEvent.isPresent() && eventCertificate.isPrimary()) {
            List<EventCertificate> eventCertificates =
                eventCertificateRepository.findAllByEvent(optionalEvent.get().getId())
                    .stream()
                    .filter(x -> x.getId() == eventCertificate.getId())
                    .toList();
            eventCertificates.forEach(x -> x.setPrimary(false));
            eventCertificateRepository.saveAll(eventCertificates);
            optionalEvent.get().setCertificate(eventCertificate);
        } else if (optionalEvent.isPresent() &&
                   optionalEvent.get().getCertificate() != null &&
                   optionalEvent.get().getCertificate().getId().equals(eventCertificate.getId())) {
            optionalEvent.get().setCertificate(null);
        }
        EventCertificate savedEventCertificate = eventCertificateRepository.save(eventCertificate);
        optionalEvent.ifPresent(eventRepository::save);
        return savedEventCertificate;
    }

    @Transactional
    public void deleteEventCertificate(EventCertificateId eventCertificateCertificateId) {
        EventCertificate eventCertificate = findById(eventCertificateCertificateId).orElseThrow(ResourceNotFoundException::new);
        eventCertificateRepository.delete(eventCertificate);
    }

    public @NonNull EventCertificate createEventCertificate(String eventCertificateCertificateName,
                                                   EventKeyDto event,
                                                   String eventCertificateLayoutDescription,
                                                   MediaFileKeyDto mediaFile,
                                                   boolean primary) {

        Optional<Event> optionalEvent =
            event != null ? eventRepository.findById(EventId.of(event.id())) : Optional.empty();

        Optional<MediaFile> optionalMediaFile =
            mediaFile != null ? mediaFileRepository.findById(MediaFileId.of(mediaFile.id())) : Optional.empty();

        EventCertificate eventCertificateCertificate = EventCertificate.of(
            EventCertificateId.empty().value(),
            eventCertificateCertificateName,
            optionalEvent.orElse(null),
            eventCertificateLayoutDescription,
            optionalMediaFile.orElse(null),
            primary);
        return eventCertificateRepository.save(eventCertificateCertificate);
    }

    public Page<EventCertificate> findAll(@Nullable String filter, @NonNull Pageable pageable) {
        return eventCertificateRepository.findAll(filter, pageable);
    }
}
