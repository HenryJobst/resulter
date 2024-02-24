package de.jobst.resulter.application;

import de.jobst.resulter.application.port.*;
import de.jobst.resulter.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;

@Service
public class EventService {

    public final PersonRepository personRepository;
    public final OrganisationRepository organisationRepository;
    private final EventRepository eventRepository;
    private final ResultListRepository resultListRepository;
    private final SplitTimeListRepository splitTimeListRepository;

    private final CupRepository cupRepository;

    public EventService(EventRepository eventRepository,
                        PersonRepository personRepository,
                        OrganisationRepository organisationRepository,
                        ResultListRepository resultListRepository,
                        SplitTimeListRepository splitTimeListRepository,
                        CupRepository cupRepository) {
        this.eventRepository = eventRepository;
        this.personRepository = personRepository;
        this.organisationRepository = organisationRepository;
        this.resultListRepository = resultListRepository;
        this.splitTimeListRepository = splitTimeListRepository;
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
                             EventStatus status,
                             Set<OrganisationId> organisationIds) {
        Optional<Event> optionalEvent = findById(id);
        if (optionalEvent.isEmpty()) {
            return null;
        }
        Event event = optionalEvent.get();
        event.update(name, startDate, status, organisationIds);
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
        Event event =
            Event.of(EventId.empty().value(), eventName, dateTime, dateTime, organisationIds, EventStatus.PLANNED);
        return eventRepository.save(event);
    }
}
