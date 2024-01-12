package de.jobst.resulter.application;

import de.jobst.resulter.application.port.CupRepository;
import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.domain.*;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    @NonNull
    public static EventConfig getEventConfig(EventShallowProxyConfig eventShallowProxyConfig) {
        EnumSet<EventConfig.ShallowEventLoads> shallowLoads = EnumSet.noneOf(EventConfig.ShallowEventLoads.class);
        if (eventShallowProxyConfig.shallowEventOrganisations()) {
            shallowLoads.add(EventConfig.ShallowEventLoads.EVENT_ORGANISATIONS);
        }
        if (eventShallowProxyConfig.shallowPersons()) {
            shallowLoads.add(EventConfig.ShallowEventLoads.PERSONS);
        }
        if (eventShallowProxyConfig.shallowOrganisations()) {
            shallowLoads.add(EventConfig.ShallowEventLoads.ORGANISATIONS);
        }
        if (eventShallowProxyConfig.shallowClassResults()) {
            shallowLoads.add(EventConfig.ShallowEventLoads.CLASS_RESULTS);
            shallowLoads.add(EventConfig.ShallowEventLoads.PERSON_RESULTS);
            shallowLoads.add(EventConfig.ShallowEventLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(EventConfig.ShallowEventLoads.SPLIT_TIMES);
            shallowLoads.add(EventConfig.ShallowEventLoads.CUP_SCORES);
            shallowLoads.add(EventConfig.ShallowEventLoads.PERSONS);
            shallowLoads.add(EventConfig.ShallowEventLoads.ORGANISATIONS);
        } else if (eventShallowProxyConfig.shallowPersonResults()) {
            shallowLoads.add(EventConfig.ShallowEventLoads.PERSON_RESULTS);
            shallowLoads.add(EventConfig.ShallowEventLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(EventConfig.ShallowEventLoads.SPLIT_TIMES);
            shallowLoads.add(EventConfig.ShallowEventLoads.CUP_SCORES);
            shallowLoads.add(EventConfig.ShallowEventLoads.PERSONS);
            shallowLoads.add(EventConfig.ShallowEventLoads.ORGANISATIONS);
        } else if (eventShallowProxyConfig.shallowPersonRaceResults()) {
            shallowLoads.add(EventConfig.ShallowEventLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(EventConfig.ShallowEventLoads.SPLIT_TIMES);
            shallowLoads.add(EventConfig.ShallowEventLoads.CUP_SCORES);
        } else if (eventShallowProxyConfig.shallowSplitTimes()) {
            shallowLoads.add(EventConfig.ShallowEventLoads.SPLIT_TIMES);
        } else if (eventShallowProxyConfig.shallowCupScores()) {
            shallowLoads.add(EventConfig.ShallowEventLoads.CUP_SCORES);
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

    public Event updateEvent(EventId id,
                             EventName name,
                             DateTime startDate,
                             Collection<OrganisationId> organisationIds) {
        EventConfig eventConfig =
            getEventConfig(new EventShallowProxyConfig(true, true, true, true, true, true, true, false));
        Optional<Event> optionalEvent = findById(id, eventConfig);
        if (optionalEvent.isEmpty()) {
            return null;
        }
        Event event = optionalEvent.get();
        event.update(name, startDate, organisationIds);
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

    @Transactional
    public Event calculateEvent(EventId id) {
        Collection<Cup> cups = cupRepository.findByEvent(id);
        if (cups.isEmpty()) {
            // no cups for this event
            return null;
        }

        EventConfig eventConfig =
            getEventConfig(new EventShallowProxyConfig(false, false, false, true, false, false, false, false));
        Optional<Event> optionalEvent = findById(id, eventConfig);
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
