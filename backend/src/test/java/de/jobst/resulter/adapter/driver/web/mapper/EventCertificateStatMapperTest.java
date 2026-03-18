package de.jobst.resulter.adapter.driver.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import de.jobst.resulter.domain.Discipline;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificateStat;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Gender;
import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonId;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EventCertificateStatMapperTest {

    @Test
    void toDtos_shouldMapCorrectly() {
        EventId eventId = EventId.of(10L);
        PersonId personId = PersonId.of(20L);

        EventCertificateStat stat = EventCertificateStat.of(1L, eventId, personId, Instant.now());
        Event event = Event.of(eventId.value(), "My Event", null, null, Set.of(), null, Discipline.getDefault(), false);
        Person person = Person.of(personId.value(), "Mustermann", "Max", null, Gender.M);

        Map<EventId, Event> eventMap = Map.of(eventId, event);
        Map<PersonId, Person> personMap = Map.of(personId, person);

        List<?> dtos = EventCertificateStatMapper.toDtos(List.of(stat), eventMap, personMap);

        assertThat(dtos).hasSize(1);
    }

    @Test
    void toDtos_shouldHandleEmptyList() {
        List<?> dtos = EventCertificateStatMapper.toDtos(List.of(), Map.of(), Map.of());

        assertThat(dtos).isEmpty();
    }
}
