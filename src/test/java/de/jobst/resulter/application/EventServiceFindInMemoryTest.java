package de.jobst.resulter.application;

import de.jobst.resulter.application.port.InMemoryCupRepository;
import de.jobst.resulter.application.port.InMemoryEventRepository;
import de.jobst.resulter.application.port.InMemoryOrganisationRepository;
import de.jobst.resulter.application.port.InMemoryPersonRepository;
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
            new InMemoryPersonRepository(),
            new InMemoryOrganisationRepository());

        assertThat(eventService.findById(EventId.of(9999L))).isEmpty();
    }

    @Test
    public void whenRepositoryIsEmptyFindOrCreateReturnsIt() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        InMemoryCupRepository cupRepository = new InMemoryCupRepository();
        InMemoryPersonRepository personRepository = new InMemoryPersonRepository();
        InMemoryOrganisationRepository organisationRepository = new InMemoryOrganisationRepository();
        EventService eventService = EventServiceFactory.createServiceWith(eventRepository,
            cupRepository,
            personRepository,
            organisationRepository);

        Event savedEvent = eventService.findOrCreate(Event.of("Test"));

        assertThat(savedEvent).isNotNull();
    }

    @Test
    public void whenRepositoryIsNotEmptyFindOrCreateReturnsItAgain() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        InMemoryCupRepository cupRepository = new InMemoryCupRepository();
        InMemoryPersonRepository personRepository = new InMemoryPersonRepository();
        InMemoryOrganisationRepository organisationRepository = new InMemoryOrganisationRepository();
        Event savedEvent = eventRepository.findOrCreate(Event.of("Test"));
        EventService eventService = EventServiceFactory.createServiceWith(eventRepository,
            cupRepository,
            personRepository,
            organisationRepository);

        Event foundEvent = eventService.findOrCreate(savedEvent);

        assertThat(foundEvent).isEqualTo(savedEvent);
    }

    @Test
    public void whenRepositoryHasEventFindByItsIdReturnsItInAnOptional() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        InMemoryCupRepository cupRepository = new InMemoryCupRepository();
        InMemoryPersonRepository personRepository = new InMemoryPersonRepository();
        InMemoryOrganisationRepository organisationRepository = new InMemoryOrganisationRepository();
        Event savedEvent = eventRepository.save(Event.of("Test"));
        EventService eventService = EventServiceFactory.createServiceWith(eventRepository,
            cupRepository,
            personRepository,
            organisationRepository);

        Optional<Event> foundEvent = eventService.findById(savedEvent.getId());

        assertThat(foundEvent).isNotEmpty();
    }

}
