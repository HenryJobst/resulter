package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.adapter.TestConfig;
import de.jobst.resulter.application.EventService;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventName;
import de.jobst.resulter.domain.EventTestDataGenerator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest(properties = {"spring.test.database.replace=NONE", "resulter.repository.inmemory=false"})
@ContextConfiguration(classes = {TestConfig.class}, loader = AnnotationConfigContextLoader.class)
@EntityScan(basePackages = {"de.jobst.resulter.adapter.driven.jdbc"})
@EnableJdbcRepositories(basePackages = "de.jobst.resulter.adapter.driven.jdbc")
@Import({EventRepositoryDataJdbcAdapter.class, EventService.class, CupRepositoryDataJdbcAdapter.class,
    PersonRepositoryDataJdbcAdapter.class, OrganisationRepositoryDataJdbcAdapter.class})
class EventServiceJpaTest {

    @Autowired
    EventService eventService;

    @Test
    @Transactional
    @Disabled
    void testUpdateExistingEvent() {

        Event event = EventTestDataGenerator.getTestEvent();
        Event savedEvent = eventService.findOrCreate(event);

        Event changedEvent = eventService.updateEvent(savedEvent.getId(),
            EventName.of("ChangedEvent"),
            savedEvent.getStartTime(),
            savedEvent.getOrganisationIds());

        assertThat(changedEvent).isNotNull();
        assertThat(changedEvent.getClassResults().value().isEmpty() ||
                   (changedEvent.getClassResults().value().size() == 1)).isTrue();

    }

}
