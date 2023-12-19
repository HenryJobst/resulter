package de.jobst.resulter.application;

import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventConfig;
import de.jobst.resulter.domain.EventId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    @Transactional
    public Event findOrCreate(Event event) {
        return eventRepository.findOrCreate(event);
    }

    @Transactional
    public Optional<Event> findById(EventId eventId, EventConfig eventConfig) {
        return eventRepository.findById(eventId, eventConfig);
    }

    @Transactional
    public List<Event> findAll(EventConfig eventConfig) {
        return eventRepository.findAll(eventConfig);
    }

    @SuppressWarnings("UnusedReturnValue")
    @Transactional
    public Event updateEvent(Event event) {
        return eventRepository.save(event);
    }
}
