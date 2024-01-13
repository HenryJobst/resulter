package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CupRepository;
import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EventService {

    public final PersonRepository personRepository;
    public final OrganisationRepository organisationRepository;
    private final EventRepository eventRepository;

    private final CupRepository cupRepository;

    public EventService(EventRepository eventRepository,
                        PersonRepository personRepository,
                        OrganisationRepository organisationRepository,
                        CupRepository cupRepository) {
        this.eventRepository = eventRepository;
        this.personRepository = personRepository;
        this.organisationRepository = organisationRepository;
        this.cupRepository = cupRepository;
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
                             Collection<OrganisationId> organisationIds) {
        Optional<Event> optionalEvent = findById(id);
        if (optionalEvent.isEmpty()) {
            return null;
        }
        Event event = optionalEvent.get();
        event.update(name, startDate, organisationIds);
        return eventRepository.save(event);
    }

    public boolean deleteEvent(EventId eventId) {
        Optional<Event> optionalEvent = findById(eventId);
        if (optionalEvent.isEmpty()) {
            return false;
        }
        Event event = optionalEvent.get();
        eventRepository.deleteEvent(event);
        return true;
    }

    @Transactional
    public Event calculateEvent(EventId id) {
        Collection<Cup> cups = cupRepository.findByEvent(id);
        if (cups.isEmpty()) {
            // no cups for this event
            return null;
        }

        Optional<Event> optionalEvent = findById(id);
        if (optionalEvent.isEmpty()) {
            // no event
            return null;
        }
        Event event = optionalEvent.get();
        Map<OrganisationId, Organisation> organisationById =
            organisationRepository.loadOrganisationTree(event.getReferencedOrganisationIds());
        cups.forEach(cup -> event.calculate(cup, organisationRepository));
        return eventRepository.save(event);
    }

}
