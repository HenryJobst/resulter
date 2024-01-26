package de.jobst.resulter.application;

import de.jobst.resulter.application.port.*;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EventServiceFindInMemoryTest {

    @Test
    public void whenRepositoryIsEmptyFindReturnsEmptyOptional() {
        EventService eventService = EventServiceFactory.createServiceWith(new InMemoryEventRepository(),
            new InMemoryCupRepository(),
            new InMemoryResultListRepository(),
            new InMemoryPersonRepository(),
            new InMemoryOrganisationRepository(),
            new InMemorySplitTimeListRepository());

        assertThat(eventService.findById(EventId.of(9999L))).isEmpty();
    }

    @Test
    public void whenRepositoryIsEmptyFindOrCreateReturnsIt() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        InMemoryCupRepository cupRepository = new InMemoryCupRepository();
        InMemoryResultListRepository resultListRepository = new InMemoryResultListRepository();
        InMemoryPersonRepository personRepository = new InMemoryPersonRepository();
        InMemoryOrganisationRepository organisationRepository = new InMemoryOrganisationRepository();
        InMemorySplitTimeListRepository splitTimeListRepository = new InMemorySplitTimeListRepository();
        EventService eventService = EventServiceFactory.createServiceWith(eventRepository,
            cupRepository,
            resultListRepository,
            personRepository,
            organisationRepository,
            splitTimeListRepository);

        Event savedEvent = eventService.findOrCreate(Event.of("Test"));

        assertThat(savedEvent).isNotNull();
    }

    @Test
    public void whenRepositoryIsNotEmptyFindOrCreateReturnsItAgain() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        InMemoryCupRepository cupRepository = new InMemoryCupRepository();
        InMemoryResultListRepository resultListRepository = new InMemoryResultListRepository();
        InMemoryPersonRepository personRepository = new InMemoryPersonRepository();
        InMemoryOrganisationRepository organisationRepository = new InMemoryOrganisationRepository();
        InMemorySplitTimeListRepository splitTimeListRepository = new InMemorySplitTimeListRepository();
        Event savedEvent = eventRepository.findOrCreate(Event.of("Test"));
        EventService eventService = EventServiceFactory.createServiceWith(eventRepository,
            cupRepository,
            resultListRepository,
            personRepository,
            organisationRepository,
            splitTimeListRepository);

        Event foundEvent = eventService.findOrCreate(savedEvent);

        assertThat(foundEvent).isEqualTo(savedEvent);
    }

    @Test
    public void whenRepositoryHasEventFindByItsIdReturnsItInAnOptional() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        InMemoryCupRepository cupRepository = new InMemoryCupRepository();
        InMemoryPersonRepository personRepository = new InMemoryPersonRepository();
        InMemoryResultListRepository resultListRepository = new InMemoryResultListRepository();
        InMemoryOrganisationRepository organisationRepository = new InMemoryOrganisationRepository();
        InMemorySplitTimeListRepository splitTimeListRepository = new InMemorySplitTimeListRepository();
        Event savedEvent = eventRepository.save(Event.of("Test"));
        EventService eventService = EventServiceFactory.createServiceWith(eventRepository,
            cupRepository,
            resultListRepository,
            personRepository,
            organisationRepository,
            splitTimeListRepository);

        Optional<Event> foundEvent = eventService.findById(savedEvent.getId());

        assertThat(foundEvent).isNotEmpty();
    }

}
