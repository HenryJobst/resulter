package de.jobst.resulter.application;

import de.jobst.resulter.application.port.*;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventServiceFindInMemoryTest {

    private EventRepository eventRepository;
    private @NotNull EventService eventService;

    @BeforeEach
    void setUp() {
        eventRepository = mock(EventRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        OrganisationRepository organisationRepository = mock(OrganisationRepository.class);
        EventCertificateRepository certificateRepository = mock(EventCertificateRepository.class);
        EventCertificateStatRepository eventCertificateStatRepository =
            mock(EventCertificateStatRepository.class);

        eventService = EventServiceFactory.createServiceWith(eventRepository,
            personRepository, organisationRepository, certificateRepository, eventCertificateStatRepository);
    }

    @Test
    public void whenRepositoryIsEmptyFindReturnsEmptyOptional() {
        EventService eventService = this.eventService;

        assertThat(eventService.findById(EventId.of(9999L))).isEmpty();
    }

    @Test
    public void whenRepositoryIsEmptyFindOrCreateReturnsIt() {
        Event testEvent = Event.of("Test");
        when(eventRepository.findOrCreate(testEvent)).thenReturn(testEvent);

        Event savedEvent = eventService.findOrCreate(testEvent);

        assertThat(savedEvent).isNotNull();
    }

    @Test
    public void whenRepositoryIsNotEmptyFindOrCreateReturnsItAgain() {
        Event testEvent = Event.of("Test");
        when(eventRepository.findOrCreate(testEvent)).thenReturn(testEvent);

        Event foundEvent = eventService.findOrCreate(testEvent);

        assertThat(foundEvent).isEqualTo(testEvent);
    }

    @Test
    public void whenRepositoryHasEventFindByItsIdReturnsItInAnOptional() {
        Event testEvent = Event.of(1L, "Test");

        when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));

        Optional<Event> foundEvent = eventService.findById(testEvent.getId());

        assertThat(foundEvent).isNotEmpty();
    }

}
