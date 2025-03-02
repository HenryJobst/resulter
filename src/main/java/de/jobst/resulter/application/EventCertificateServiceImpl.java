package de.jobst.resulter.application;

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
public class EventCertificateServiceImpl implements EventCertificateService {

    public final PersonRepository personRepository;
    public final OrganisationRepository organisationRepository;
    private final EventCertificateRepository eventCertificateRepository;

    private final EventRepository eventRepository;
    private final MediaFileRepository mediaFileRepository;

    public EventCertificateServiceImpl(
            EventCertificateRepository eventCertificateRepository,
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

    @Override
    public Optional<EventCertificate> findById(EventCertificateId eventCertificateCertificateCertificateId) {
        return eventCertificateRepository.findById(eventCertificateCertificateCertificateId);
    }

    @Override
    public List<EventCertificate> findAll() {
        return eventCertificateRepository.findAll();
    }

    @NonNull
    @Override
    public EventCertificate updateEventCertificate(
        EventCertificateId id,
        EventCertificateName name,
        EventId event,
        EventCertificateLayoutDescription eventCertificateLayoutDescription,
        MediaFileId mediaFile,
        boolean primary) {
        EventCertificate eventCertificate = findById(id).orElseThrow(ResourceNotFoundException::new);
        Optional<Event> optionalEvent =
                event != null ? eventRepository.findById(event) : Optional.empty();

        Optional<MediaFile> optionalMediaFile =
                mediaFile != null ? mediaFileRepository.findById(mediaFile) : Optional.empty();

        eventCertificate.update(
                name,
                optionalEvent.map(Event::getId).orElse(null),
                eventCertificateLayoutDescription,
                optionalMediaFile.map(MediaFile::getId).orElse(null),
                primary);

        if (optionalEvent.isPresent() && eventCertificate.isPrimary()) {
            List<EventCertificate> eventCertificates =
                    eventCertificateRepository
                            .findAllByEvent(optionalEvent.get().getId())
                            .stream()
                            .filter(x -> x.getId() == eventCertificate.getId())
                            .toList();
            eventCertificates.forEach(x -> x.setPrimary(false));
            eventCertificateRepository.saveAll(eventCertificates);
            optionalEvent.get().setCertificate(eventCertificate.getId());
        } else if (optionalEvent.isPresent()
                && optionalEvent.get().getCertificate() != null
                && optionalEvent.get().getCertificate().equals(eventCertificate.getId())) {
            optionalEvent.get().setCertificate(null);
        }
        EventCertificate savedEventCertificate = eventCertificateRepository.save(eventCertificate);
        optionalEvent.ifPresent(eventRepository::save);
        return savedEventCertificate;
    }

    @Transactional
    @Override
    public void deleteEventCertificate(EventCertificateId eventCertificateCertificateId) {
        EventCertificate eventCertificate =
                findById(eventCertificateCertificateId).orElseThrow(ResourceNotFoundException::new);
        eventCertificateRepository.delete(eventCertificate);
    }

    @NonNull
    @Override
    public EventCertificate createEventCertificate(
        String eventCertificateCertificateName,
        EventId event,
        String eventCertificateLayoutDescription,
        MediaFileId mediaFile,
        boolean primary) {

        Optional<Event> optionalEvent =
                event != null ? eventRepository.findById(event) : Optional.empty();

        Optional<MediaFile> optionalMediaFile =
                mediaFile != null ? mediaFileRepository.findById(mediaFile) : Optional.empty();

        EventCertificate eventCertificateCertificate = EventCertificate.of(
                EventCertificateId.empty().value(),
                eventCertificateCertificateName,
                optionalEvent.map(Event::getId).orElse(null),
                eventCertificateLayoutDescription,
                optionalMediaFile.map(MediaFile::getId).orElse(null),
                primary);
        return eventCertificateRepository.save(eventCertificateCertificate);
    }

    @Override
    public Page<EventCertificate> findAll(@Nullable String filter, @NonNull Pageable pageable) {
        return eventCertificateRepository.findAll(filter, pageable);
    }

    @Override
    public EventCertificate getById(EventCertificateId id) {
        return eventCertificateRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
    }
}
