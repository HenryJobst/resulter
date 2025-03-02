package de.jobst.resulter.adapter.driven.inmemory;

import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryEventRepositoryTest {

    EventRepository inMemoryEventRepository = new InMemoryEventRepository();

    @Test
    public void savedEventCanBeFoundByItsId() {
        Event event = Event.of("test event");

        Event savedEvent = inMemoryEventRepository.save(event);

        Optional<Event> found = inMemoryEventRepository.findById(Objects.requireNonNull(savedEvent.getId()));

        assertThat(found).isPresent().get().extracting(Event::getName).isEqualTo(EventName.of("test event"));
    }

    @Test
    public void newRepositoryReturnsEmptyForFindAll() {
        List<Event> events = inMemoryEventRepository.findAll();

        assertThat(events).isEmpty();
    }

    @Test
    public void twoSavedEventsBothReturnedByFindAll() {
        Event one = Event.of("one");
        Event two = Event.of("two");

        inMemoryEventRepository.save(one);
        inMemoryEventRepository.save(two);

        List<Event> allEvents = inMemoryEventRepository.findAll();
        assertThat(allEvents).hasSize(2);

    }

    @Test
    void findOrCreateWithNonExisting() {
        Event event = Event.of("test event");

        Event savedEvent = inMemoryEventRepository.findOrCreate(event);

        Optional<Event> found = inMemoryEventRepository.findById(Objects.requireNonNull(savedEvent.getId()));

        assertThat(found).isPresent().get().extracting(Event::getName).isEqualTo(EventName.of("test event"));
    }

    @Test
    void findOrCreateWithExisting() {
        Event event = Event.of("test event");
        inMemoryEventRepository.save(event);

        Event savedEvent = inMemoryEventRepository.findOrCreate(event);

        Optional<Event> found = inMemoryEventRepository.findById(Objects.requireNonNull(savedEvent.getId()));

        assertThat(found).isPresent().get().extracting(Event::getName).isEqualTo(EventName.of("test event"));
    }
}
