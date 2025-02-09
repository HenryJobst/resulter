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

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class EventService {

    public final PersonRepository personRepository;
    public final OrganisationRepository organisationRepository;
    private final EventRepository eventRepository;

    private final EventCertificateRepository eventCertificateRepository;
    private final EventCertificateStatRepository eventCertificateStatRepository;

    public EventService(EventRepository eventRepository,
                        PersonRepository personRepository,
                        OrganisationRepository organisationRepository,
                        EventCertificateRepository eventCertificateRepository,
                        EventCertificateStatRepository eventCertificateStatRepository) {
        this.eventRepository = eventRepository;
        this.personRepository = personRepository;
        this.organisationRepository = organisationRepository;
        this.eventCertificateRepository = eventCertificateRepository;
        this.eventCertificateStatRepository = eventCertificateStatRepository;
    }

    public Event findOrCreate(Event event) {
        return eventRepository.findOrCreate(event);
    }

    public Optional<Event> findById(EventId eventId) {
        return eventRepository.findById(eventId);
    }

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public @NonNull Event updateEvent(EventId id,
                             @NonNull EventName name,
                             @Nullable DateTime startDate,
                             @NonNull EventStatus status,
                             @NonNull Collection<OrganisationId> organisationIds,
                             @Nullable EventCertificateId certificateId) {
        Event event = findById(id).orElseThrow(ResourceNotFoundException::new);
        List<Organisation> organisations = organisationRepository.findByIds(organisationIds);
        EventCertificate certificate =
            certificateId != null ? eventCertificateRepository.findById(certificateId).orElseThrow() : null;
        event.update(name, startDate, status, organisations, certificate);
        if (certificate != null) {
            List<EventCertificate> eventCertificates = eventCertificateRepository.findAllByEvent(event.getId());
            eventCertificates.forEach(eventCertificate -> {
                if (eventCertificate.getId().equals(certificateId)) {
                    eventCertificate.setPrimary(true);
                } else {
                    eventCertificate.setPrimary(false);
                }
            });
            eventCertificateRepository.saveAll(eventCertificates);
            eventCertificateRepository.save(certificate);
        }
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(EventId eventId) {
        Event event = findById(eventId).orElseThrow(ResourceNotFoundException::new);
        eventCertificateStatRepository.deleteAllByEventId(eventId);
        eventRepository.deleteEvent(event);
    }


    public Event createEvent(String eventName, ZonedDateTime dateTime, Set<OrganisationId> organisationIds) {
        List<Organisation> organisations = organisationRepository.findByIds(organisationIds);
        Event event =
            Event.of(EventId.empty().value(), eventName, dateTime, dateTime, organisations, EventStatus.PLANNED);
        return eventRepository.save(event);
    }

    public Page<Event> findAll(@Nullable String filter, @NonNull Pageable pageable) {
        return eventRepository.findAll(filter, pageable);
    }

}
