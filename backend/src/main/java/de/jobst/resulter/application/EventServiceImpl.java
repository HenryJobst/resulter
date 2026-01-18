package de.jobst.resulter.application;

import de.jobst.resulter.application.port.*;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventServiceImpl implements EventService {

    public final PersonRepository personRepository;
    public final OrganisationRepository organisationRepository;
    private final EventRepository eventRepository;

    private final EventCertificateRepository eventCertificateRepository;
    private final EventCertificateStatRepository eventCertificateStatRepository;

    public EventServiceImpl(
            EventRepository eventRepository,
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

    @Override
    public Event findOrCreate(Event event) {
        return eventRepository.findOrCreate(event);
    }

    @Override
    public Event getById(EventId eventId) {
        return eventRepository.findById(eventId).orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public List<Event> getByIds(Collection<EventId> eventIds) {
        List<Event> events = eventRepository.findAllById(eventIds);
        if (events.size() != eventIds.size()) {
            throw new ResourceNotFoundException();
        }
        return events;
    }

    @Override
    public Optional<Event> findById(EventId eventId) {
        return eventRepository.findById(eventId);
    }

    @Override
    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    @Override
    public List<Event> findAllById(Collection<EventId> eventIds) {
        return eventRepository.findAllById(eventIds);
    }

    @Override
    public Map<EventId, Event> findAllByIdAsMap(Set<EventId> eventIds) {
        return eventRepository.findAllById(eventIds).stream().collect(Collectors.toMap(Event::getId, event -> event));
    }

    @Override
    public Event updateEvent(
            EventId id,
            EventName name,
            @Nullable DateTime startDate,
            EventStatus status,
            Collection<OrganisationId> organisationIds,
            @Nullable EventCertificateId certificateId,
            Discipline discipline,
            boolean aggregatedScore) {
        Event event = findById(id).orElseThrow(ResourceNotFoundException::new);
        List<Organisation> organisations = organisationRepository.findByIds(organisationIds);
        EventCertificate certificate = certificateId != null
                ? eventCertificateRepository.findById(certificateId).orElseThrow()
                : null;
        event.update(
                name,
                startDate,
                status,
                organisations.stream().map(Organisation::getId).toList(),
                certificate != null ? certificate.getId() : null,
                discipline,
                aggregatedScore);
        if (certificate != null) {
            List<EventCertificate> eventCertificates = eventCertificateRepository.findAllByEvent(event.getId());
            eventCertificates.forEach(eventCertificate ->
                    eventCertificate.setPrimary(eventCertificate.getId().equals(certificateId)));
            eventCertificateRepository.saveAll(eventCertificates);
            eventCertificateRepository.save(certificate);
        }
        return eventRepository.save(event);
    }

    @Transactional
    @Override
    public void deleteEvent(EventId eventId) {
        Event event = findById(eventId).orElseThrow(ResourceNotFoundException::new);
        eventCertificateStatRepository.deleteAllByEventId(eventId);
        eventRepository.deleteEvent(event);
    }

    @Override
    public Event createEvent(
            String eventName,
            ZonedDateTime dateTime,
            Set<OrganisationId> organisationIds,
            String discipline,
            Boolean aggregatedScore) {
        List<Organisation> organisations = organisationRepository.findByIds(organisationIds);
        Event event = Event.of(
                EventId.empty().value(),
                eventName,
                dateTime,
                dateTime,
                organisations.stream().map(Organisation::getId).toList(),
                EventStatus.PLANNED,
                Discipline.fromValue(discipline),
                aggregatedScore);
        return eventRepository.save(event);
    }

    @Override
    public Page<Event> findAll(@Nullable String filter, Pageable pageable) {
        return eventRepository.findAll(filter, pageable);
    }
}
