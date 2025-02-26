package de.jobst.resulter.application;

import de.jobst.resulter.adapter.driven.inmemory.*;
import de.jobst.resulter.application.port.*;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EventServiceFindInMemoryTest {

    @Test
    public void whenRepositoryIsEmptyFindReturnsEmptyOptional() {
        EventService eventService = EventServiceFactory.createServiceWith(new InMemoryEventRepository(),
            new InMemoryPersonRepository(),
            new InMemoryOrganisationRepository(),
            new InMemoryEventCertificateRepository(),
            new InMemoryEventCertificateStatRepository());

        assertThat(eventService.findById(EventId.of(9999L))).isEmpty();
    }

    @Test
    public void whenRepositoryIsEmptyFindOrCreateReturnsIt() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        InMemoryPersonRepository personRepository = new InMemoryPersonRepository();
        InMemoryOrganisationRepository organisationRepository = new InMemoryOrganisationRepository();
        InMemoryEventCertificateRepository certificateRepository = new InMemoryEventCertificateRepository();
        InMemoryEventCertificateStatRepository eventCertificateStatRepository =
            new InMemoryEventCertificateStatRepository();
        EventService eventService = EventServiceFactory.createServiceWith(eventRepository,
            personRepository,
            organisationRepository,
            certificateRepository,
            eventCertificateStatRepository);

        Event savedEvent = eventService.findOrCreate(Event.of("Test"));

        assertThat(savedEvent).isNotNull();
    }

    @Test
    public void whenRepositoryIsNotEmptyFindOrCreateReturnsItAgain() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        InMemoryPersonRepository personRepository = new InMemoryPersonRepository();
        InMemoryOrganisationRepository organisationRepository = new InMemoryOrganisationRepository();
        InMemoryEventCertificateRepository certificateRepository = new InMemoryEventCertificateRepository();
        InMemoryEventCertificateStatRepository eventCertificateStatRepository =
            new InMemoryEventCertificateStatRepository();
        Event savedEvent = eventRepository.findOrCreate(Event.of("Test"));
        EventService eventService = EventServiceFactory.createServiceWith(eventRepository,
            personRepository,
            organisationRepository,
            certificateRepository,
            eventCertificateStatRepository);

        Event foundEvent = eventService.findOrCreate(savedEvent);

        assertThat(foundEvent).isEqualTo(savedEvent);
    }

    @Test
    public void whenRepositoryHasEventFindByItsIdReturnsItInAnOptional() {
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        InMemoryPersonRepository personRepository = new InMemoryPersonRepository();
        InMemoryOrganisationRepository organisationRepository = new InMemoryOrganisationRepository();
        InMemoryEventCertificateRepository certificateRepository = new InMemoryEventCertificateRepository();
        InMemoryEventCertificateStatRepository eventCertificateStatRepository =
            new InMemoryEventCertificateStatRepository();
        Event savedEvent = eventRepository.save(Event.of("Test"));
        EventService eventService = EventServiceFactory.createServiceWith(eventRepository,
            personRepository,
            organisationRepository,
            certificateRepository,
            eventCertificateStatRepository);

        Optional<Event> foundEvent = eventService.findById(savedEvent.getId());

        assertThat(foundEvent).isNotEmpty();
    }

}
