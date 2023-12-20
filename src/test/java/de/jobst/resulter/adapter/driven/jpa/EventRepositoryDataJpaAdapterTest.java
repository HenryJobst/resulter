package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.adapter.TestConfig;
import de.jobst.resulter.application.port.EventRepository;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventConfig;
import de.jobst.resulter.domain.EventName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.test.database.replace=NONE",
        "resulter.repository.inmemory=false"})
@ContextConfiguration(
        classes = {TestConfig.class},
        loader = AnnotationConfigContextLoader.class)
@EntityScan(basePackages = {"de.jobst.resulter.adapter.driven.jpa"})
@EnableJpaRepositories(basePackages = "de.jobst.resulter.adapter.driven.jpa")
@Import(EventRepositoryDataJpaAdapter.class)
class EventRepositoryDataJpaAdapterTest {

    @Autowired
    EventRepository eventRepository;

    @Test
    @Transactional
    public void savedEventCanBeFoundByItsId() {
        String eventName = "test event";
        Event event = Event.of(eventName);

        Event savedEvent = eventRepository.save(event);

        Optional<Event>
                found =
                eventRepository.findById(Objects.requireNonNull(savedEvent.getId()), EventConfig.full());

        assertThat(found)
                .isPresent()
                .get()
                .extracting(Event::getName)
                .isEqualTo(EventName.of(eventName));
    }

    @Test
    @Transactional
    public void newRepositoryReturnsEmptyForFindAll() {
        List<Event> events = eventRepository.findAll(EventConfig.full());

        assertThat(events).isEmpty();
    }

    @Test
    @Transactional
    public void twoSavedEventsBothReturnedByFindAll() {
        Event one = Event.of("one");
        Event two = Event.of("two");

        eventRepository.save(one);
        eventRepository.save(two);

        List<Event> allEvents = eventRepository.findAll(EventConfig.full());
        assertThat(allEvents)
                .hasSize(2);
    }

    @Test
    @Transactional
    void findOrCreateWithNonExisting() {
        String eventName = "test event";
        Event event = Event.of(eventName);

        Event savedEvent = eventRepository.findOrCreate(event);

        Optional<Event>
                found =
                eventRepository.findById(Objects.requireNonNull(savedEvent.getId()), EventConfig.full());

        assertThat(found)
                .isPresent()
                .get()
                .extracting(Event::getName)
                .isEqualTo(EventName.of(eventName));
    }

    @Test
    @Transactional
    void findOrCreateWithExisting() {
        String eventName = "test event";
        Event event = Event.of(eventName);
        eventRepository.save(event);

        Event savedEvent = eventRepository.findOrCreate(event);

        Optional<Event>
                found =
                eventRepository.findById(Objects.requireNonNull(savedEvent.getId()), EventConfig.full());

        assertThat(found)
                .isPresent()
                .get()
                .extracting(Event::getName)
                .isEqualTo(EventName.of(eventName));
    }

}