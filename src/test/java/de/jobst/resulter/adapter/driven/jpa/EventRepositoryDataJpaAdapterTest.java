package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.adapter.TestConfig;
import de.jobst.resulter.domain.Event;
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
    EventRepositoryDataJpaAdapter eventRepositoryAdapter;

    @Test
    @Transactional
    public void savedEventCanBeFoundByItsId() {
        String eventName = "test event";
        Event event = Event.of(eventName);

        Event savedEvent = eventRepositoryAdapter.save(event);

        Optional<Event> found = eventRepositoryAdapter.findById(Objects.requireNonNull(savedEvent.getId()));

        assertThat(found)
                .isPresent()
                .get()
                .extracting(Event::getName)
                .isEqualTo(EventName.of(eventName));
    }

    @Test
    @Transactional
    public void newRepositoryReturnsEmptyForFindAll() {
        List<Event> events = eventRepositoryAdapter.findAll();

        assertThat(events).isEmpty();
    }

    @Test
    @Transactional
    public void twoSavedEventsBothReturnedByFindAll() {
        Event one = Event.of("one");
        Event two = Event.of("two");

        eventRepositoryAdapter.save(one);
        eventRepositoryAdapter.save(two);

        List<Event> allEvents = eventRepositoryAdapter.findAll();
        assertThat(allEvents)
                .hasSize(2);
    }

    @Test
    @Transactional
    void findOrCreateWithNonExisting() {
        String eventName = "test event";
        Event event = Event.of(eventName);

        Event savedEvent = eventRepositoryAdapter.findOrCreate(event);

        Optional<Event> found = eventRepositoryAdapter.findById(Objects.requireNonNull(savedEvent.getId()));

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
        eventRepositoryAdapter.save(event);

        Event savedEvent = eventRepositoryAdapter.findOrCreate(event);

        Optional<Event> found = eventRepositoryAdapter.findById(Objects.requireNonNull(savedEvent.getId()));

        assertThat(found)
                .isPresent()
                .get()
                .extracting(Event::getName)
                .isEqualTo(EventName.of(eventName));
    }

}