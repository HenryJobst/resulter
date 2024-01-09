package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CupRepository;
import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.domain.*;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    public final PersonRepository personRepository;
    public final OrganisationRepository organisationRepository;
    private final EventRepository eventRepository;

    private final CupRepository cupRepository;

    public EventService(EventRepository eventRepository,
                        PersonRepository personRepository,
                        OrganisationRepository organisationRepository, CupRepository cupRepository) {
        this.eventRepository = eventRepository;
        this.personRepository = personRepository;
        this.organisationRepository = organisationRepository;
        this.cupRepository = cupRepository;
    }

    @NonNull
    public static EventConfig getEventConfig(Boolean shallowClassResults,
                                             Boolean shallowPersonResults,
                                             Boolean shallowPersonRaceResults,
                                             Boolean shallowSplitTimes,
                                             Boolean shallowPersons,
                                             Boolean shallowOrganisations,
                                             Boolean shallowEventOrganisations) {
        EnumSet<EventConfig.ShallowEventLoads> shallowLoads = EnumSet.noneOf(EventConfig.ShallowEventLoads.class);
        if (shallowEventOrganisations) {
            shallowLoads.add(EventConfig.ShallowEventLoads.EVENT_ORGANISATIONS);
        }
        if (shallowPersons) {
            shallowLoads.add(EventConfig.ShallowEventLoads.PERSONS);
        }
        if (shallowOrganisations) {
            shallowLoads.add(EventConfig.ShallowEventLoads.ORGANISATIONS);
        }
        if (shallowClassResults) {
            shallowLoads.add(EventConfig.ShallowEventLoads.CLASS_RESULTS);
            shallowLoads.add(EventConfig.ShallowEventLoads.PERSON_RESULTS);
            shallowLoads.add(EventConfig.ShallowEventLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(EventConfig.ShallowEventLoads.SPLIT_TIMES);
            shallowLoads.add(EventConfig.ShallowEventLoads.PERSONS);
            shallowLoads.add(EventConfig.ShallowEventLoads.ORGANISATIONS);
        } else if (shallowPersonResults) {
            shallowLoads.add(EventConfig.ShallowEventLoads.PERSON_RESULTS);
            shallowLoads.add(EventConfig.ShallowEventLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(EventConfig.ShallowEventLoads.SPLIT_TIMES);
            shallowLoads.add(EventConfig.ShallowEventLoads.PERSONS);
            shallowLoads.add(EventConfig.ShallowEventLoads.ORGANISATIONS);
        } else if (shallowPersonRaceResults) {
            shallowLoads.add(EventConfig.ShallowEventLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(EventConfig.ShallowEventLoads.SPLIT_TIMES);
        } else if (shallowSplitTimes) {
            shallowLoads.add(EventConfig.ShallowEventLoads.SPLIT_TIMES);
        }
        return EventConfig.of(shallowLoads);
    }

    public Event findOrCreate(Event event) {
        return eventRepository.findOrCreate(event);
    }

    public Optional<Event> findById(EventId eventId, EventConfig eventConfig) {
        return eventRepository.findById(eventId, eventConfig);
    }

    public List<Event> findAll(EventConfig eventConfig) {
        return eventRepository.findAll(eventConfig);
    }

    public Event updateEvent(EventId id, EventName name, DateTime startDate, Organisations organisations) {
        EventConfig eventConfig = getEventConfig(true,
                true,
                true,
                true,
                true,
                true,
                false);
        Optional<Event> optionalEvent = findById(id, eventConfig);
        if (optionalEvent.isEmpty()) {
            return null;
        }
        Event event = optionalEvent.get();
        event.update(name, startDate, organisations);
        return eventRepository.save(event);
    }

    public boolean deleteEvent(EventId eventId) {
        Optional<Event> optionalEvent = findById(eventId, EventConfig.full());
        if (optionalEvent.isEmpty()) {
            return false;
        }
        Event event = optionalEvent.get();
        eventRepository.deleteEvent(event);
        return true;
    }

    public Event calculateEvent(EventId id) {
        EventConfig eventConfig = getEventConfig(false,
                false,
                false,
                true,
                false,
                false,
                true);
        Optional<Event> optionalEvent = findById(id, eventConfig);
        if (optionalEvent.isEmpty()) {
            return null;
        }
        Event event = optionalEvent.get();
        Collection<Cup> cups = cupRepository.findByEvent(event);
        cups.forEach(event::calculate);
        return eventRepository.save(event);
    }
}
