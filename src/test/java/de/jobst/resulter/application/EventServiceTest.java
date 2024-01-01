package de.jobst.resulter.application;

import de.jobst.resulter.adapter.TestConfig;
import de.jobst.resulter.adapter.driven.jpa.*;
import de.jobst.resulter.domain.Event;
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

        DboResolvers dboResolvers = new DboResolvers(
                null,
                null,
                null,
                null
        );

        // Entität anlegen und wieder entladen
        Event event = EventTestDataGenerator.getTestEvent();
        Event savedEvent = eventService.findOrCreate(event);
        entityManager.detach(EventDbo.from(savedEvent, (e) -> null, dboResolvers));

        Event changedEvent =
                eventService.updateEvent(savedEvent.getId(),
                        EventName.of("ChangedEvent"),
                        savedEvent.getStartTime(),
                        savedEvent.getOrganisations().get());

        assertThat(changedEvent).isNotNull();
        assertThat(changedEvent.getClassResults().isEmpty() ||
                (changedEvent.getClassResults().isLoaded() &&
                        changedEvent.getClassResults().get().value().size() == 1)).isTrue();

    }

}