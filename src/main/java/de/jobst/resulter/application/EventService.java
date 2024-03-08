package de.jobst.resulter.application;

import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.domain.*;
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

    public EventService(EventRepository eventRepository,
                        PersonRepository personRepository,
                        OrganisationRepository organisationRepository) {
        this.eventRepository = eventRepository;
        this.personRepository = personRepository;
        this.organisationRepository = organisationRepository;
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

    public Event updateEvent(EventId id,
                             EventName name,
                             DateTime startDate,
                             EventStatus status,
                             Collection<OrganisationId> organisationIds) {
        Optional<Event> optionalEvent = findById(id);
        if (optionalEvent.isEmpty()) {
            return null;
        }
        Event event = optionalEvent.get();
        List<Organisation> organisations = organisationRepository.findByIds(organisationIds);
        event.update(name, startDate, status, organisations);
        return eventRepository.save(event);
    }

    @Transactional
    public boolean deleteEvent(EventId eventId) {
        Optional<Event> optionalEvent = findById(eventId);
        if (optionalEvent.isEmpty()) {
            return false;
        }
        eventRepository.deleteEvent(optionalEvent.get());
        return true;
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
