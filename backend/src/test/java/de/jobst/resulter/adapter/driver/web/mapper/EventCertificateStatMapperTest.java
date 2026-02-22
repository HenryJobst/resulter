package de.jobst.resulter.adapter.driver.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.PersonService;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EventCertificateStatMapperTest {

    private EventService eventService;
    private PersonService personService;
    private EventCertificateStatMapper mapper;

    @BeforeEach
    void setUp() {
        eventService = Mockito.mock(EventService.class);
        personService = Mockito.mock(PersonService.class);
        mapper = new EventCertificateStatMapper(eventService, personService);
    }

    @Test
    void toDtos_shouldBatchLoadEventsAndPersons() {
        EventId eventId = EventId.of(10L);
        PersonId personId = PersonId.of(20L);

        EventCertificateStat stat = EventCertificateStat.of(1L, eventId, personId, Instant.now());
        Event event = Event.of(eventId.value(), "My Event");
        Person person = Person.of(personId.value(), "Mustermann", "Max", null, Gender.M);

        when(eventService.findAllByIdAsMap(Set.of(eventId))).thenReturn(Map.of(eventId, event));
        when(personService.findAllByIdAsMap(Set.of(personId))).thenReturn(Map.of(personId, person));

        List<?> dtos = mapper.toDtos(List.of(stat));

        assertThat(dtos).hasSize(1);
        verify(eventService).findAllByIdAsMap(Set.of(eventId));
        verify(personService).findAllByIdAsMap(Set.of(personId));
        verify(eventService, never()).getById(eventId);
        verify(personService, never()).getById(personId);
    }

    @Test
    void toDtos_shouldHandleEmptyList() {
        List<?> dtos = mapper.toDtos(List.of());

        assertThat(dtos).isEmpty();
        verify(eventService, never()).findAllByIdAsMap(Set.of());
        verify(personService, never()).findAllByIdAsMap(Set.of());
    }
}
