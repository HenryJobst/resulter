package de.jobst.resulter.application;

import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.domain.*;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
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

    @NonNull
    public static EventConfig getEventConfig(Boolean shallowClassResults,
                                             Boolean shallowPersonResults,
                                             Boolean shallowPersonRaceResults,
                                             Boolean shallowSplitTimes,
                                             Boolean shallowPersons,
                                             Boolean shallowOrganisations,
                                             Boolean shallowEventOrganisations) {
        EnumSet<EventConfig.ShallowLoads> shallowLoads = EnumSet.noneOf(EventConfig.ShallowLoads.class);
        if (shallowEventOrganisations) {
            shallowLoads.add(EventConfig.ShallowLoads.EVENT_ORGANISATIONS);
        }
        if (shallowPersons) {
            shallowLoads.add(EventConfig.ShallowLoads.PERSONS);
        }
        if (shallowOrganisations) {
            shallowLoads.add(EventConfig.ShallowLoads.ORGANISATIONS);
        }
        if (shallowClassResults) {
            shallowLoads.add(EventConfig.ShallowLoads.CLASS_RESULTS);
            shallowLoads.add(EventConfig.ShallowLoads.PERSON_RESULTS);
            shallowLoads.add(EventConfig.ShallowLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(EventConfig.ShallowLoads.SPLIT_TIMES);
            shallowLoads.add(EventConfig.ShallowLoads.PERSONS);
            shallowLoads.add(EventConfig.ShallowLoads.ORGANISATIONS);
        } else if (shallowPersonResults) {
            shallowLoads.add(EventConfig.ShallowLoads.PERSON_RESULTS);
            shallowLoads.add(EventConfig.ShallowLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(EventConfig.ShallowLoads.SPLIT_TIMES);
            shallowLoads.add(EventConfig.ShallowLoads.PERSONS);
            shallowLoads.add(EventConfig.ShallowLoads.ORGANISATIONS);
        } else if (shallowPersonRaceResults) {
            shallowLoads.add(EventConfig.ShallowLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(EventConfig.ShallowLoads.SPLIT_TIMES);
        } else if (shallowSplitTimes) {
            shallowLoads.add(EventConfig.ShallowLoads.SPLIT_TIMES);
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

}
