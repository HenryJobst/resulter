package de.jobst.resulter.application;

import de.jobst.resulter.adapter.TestConfig;
import de.jobst.resulter.adapter.driven.jpa.EventRepositoryDataJpaAdapter;
import de.jobst.resulter.adapter.driven.jpa.OrganisationRepositoryDataJpaAdapter;
import de.jobst.resulter.adapter.driven.jpa.PersonRepositoryDataJpaAdapter;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventConfig;
import de.jobst.resulter.domain.EventName;
import de.jobst.resulter.domain.EventTestDataGenerator;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.test.database.replace=NONE",
        "resulter.repository.inmemory=false"})
@ContextConfiguration(
        classes = {TestConfig.class},
        loader = AnnotationConfigContextLoader.class)
@EntityScan(basePackages = {"de.jobst.resulter.adapter.driven.jpa"})
@EnableJpaRepositories(basePackages = "de.jobst.resulter.adapter.driven.jpa")
@Import({EventRepositoryDataJpaAdapter.class,
        EventService.class,
        PersonRepositoryDataJpaAdapter.class,
        OrganisationRepositoryDataJpaAdapter.class})
class EventServiceTest {

    @Autowired
    EventService eventService;

    @Autowired
    EntityManager entityManager;

    @Test
    @Transactional
    void testUpdateExistingEvent() {
        // Entität anlegen und wieder entladen
        Event event = EventTestDataGenerator.getTestEvent();
        Event savedEvent = eventService.findOrCreate(event);
        // Entität flach laden
        EventConfig eventConfig = EventConfig.full();
        eventConfig.shallowLoads().add(EventConfig.ShallowLoads.CLASS_RESULTS);
        Optional<Event> eventToChange = eventService.findById(savedEvent.getId(), eventConfig);
        assertThat(eventToChange).isPresent();

        Event
                changedEvent =
                eventService.updateEvent(eventToChange.get().getId(),
                        EventName.of("ChangedEvent"),
                        eventToChange.get().getStartTime());

        assertThat(changedEvent.getClassResults().isEmpty() ||
                (changedEvent.getClassResults().isLoaded() &&
                        changedEvent.getClassResults().get().value().size() == 1)).isTrue();

    }

}