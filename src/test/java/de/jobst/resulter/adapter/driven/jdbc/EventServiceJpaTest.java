package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.TestConfig;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventName;
import de.jobst.resulter.domain.EventTestDataGenerator;
import de.jobst.resulter.domain.TestEventResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@DataJdbcTest(properties = {"spring.test.database.replace=NONE", "resulter.repository.inmemory=false"})
@ContextConfiguration(
        classes = {TestConfig.class},
        loader = AnnotationConfigContextLoader.class)
@EntityScan(basePackages = {"de.jobst.resulter.adapter.driven.jdbc"})
@EnableJdbcRepositories(basePackages = "de.jobst.resulter.adapter.driven.jdbc")
@Import({
    EventRepositoryDataJdbcAdapter.class,
    EventService.class,
    CupRepositoryDataJdbcAdapter.class,
    PersonRepositoryDataJdbcAdapter.class,
    OrganisationRepositoryDataJdbcAdapter.class
})
class EventServiceJpaTest {

    private final EventService eventService;

    EventServiceJpaTest(EventService eventService) {this.eventService = eventService;}

    @Test
    @Transactional
    @Disabled
    void testUpdateExistingEvent() {

        TestEventResult testEventResult = EventTestDataGenerator.getTestEvent();
        Event event = testEventResult.event();

        Event savedEvent = eventService.findOrCreate(event);

        Event changedEvent = eventService.updateEvent(
                savedEvent.getId(),
                EventName.of("ChangedEvent"),
                savedEvent.getStartTime(),
                savedEvent.getEventState(),
                savedEvent.getOrganisationIds(),
                savedEvent.getCertificate() != null ? savedEvent.getCertificate() : null);

        assertThat(changedEvent).isNotNull();
        /* TODO transform test to new domain entity and dbo structure
        assertThat(changedEvent.getClassResults().value().isEmpty() ||
                   (changedEvent.getClassResults().value().size() == 1)).isTrue();

         */
    }
}
