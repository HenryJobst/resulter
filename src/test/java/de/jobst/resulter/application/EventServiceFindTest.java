package de.jobst.resulter.application;

import de.jobst.resulter.adapter.out.jpa.inmem.InMemoryEventRepository;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EventServiceFindTest {
    @Test
    public void whenRepositoryIsEmptyFindReturnsEmptyOptional() {
        EventService eventService = EventServiceFactory.createServiceWith(new InMemoryEventRepository());

        assertThat(eventService.findById(EventId.of(9999))).isEmpty();
    }

    @Test
    public void whenRepositoryIsEmptyFindOrCreateReturnsIt() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        EventService eventService = EventServiceFactory.createServiceWith(eventRepository);

        Event savedEvent = eventService.findOrCreate(Event.of("Test", null));

        assertThat(savedEvent).isNotNull();
    }

    @Test
    public void whenRepositoryIsNotEmptyFindOrCreateReturnsItAgain() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        Event savedEvent = eventRepository.findOrCreate(Event.of("Test", null));
        EventService eventService = EventServiceFactory.createServiceWith(eventRepository);

        Event foundEvent = eventService.findOrCreate(savedEvent);

        assertThat(foundEvent).isEqualTo(savedEvent);
    }
    
    @Test
    public void whenRepositoryHasEventFindByItsIdReturnsItInAnOptional() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        Event savedEvent = eventRepository.save(Event.of("Test", null));
        EventService eventService = EventServiceFactory.createServiceWith(eventRepository);

        Optional<Event> foundEvent = eventService.findById(savedEvent.getId());

        assertThat(foundEvent).isNotEmpty();
    }

}