package de.jobst.resulter.adapter.out.jpa;

import de.jobst.resulter.domain.Event;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventEntityMappingTest {

    @Test
    public void databaseEntityToDomainIsMappedCorrectly() throws Exception {
        EventEntity eventDbo = new EventEntity();
        eventDbo.setId(19L);
        eventDbo.setName("Entity");

        Event event = eventDbo.asEvent();

        assertThat(event.getId().value()).isEqualTo(19L);
        assertThat(event.getName().value())
                .isEqualTo("Entity");
    }

    @Test
    public void domainToDatabaseEntityIsMappedCorrectly() throws Exception {
        Event event = Event.of("Domain", null);

        EventEntity entity = EventEntity.from(event);

        assertThat(entity.getName())
                .isEqualTo("Domain");
    }

}