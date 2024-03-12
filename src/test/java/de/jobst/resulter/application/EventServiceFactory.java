package de.jobst.resulter.application;

import de.jobst.resulter.application.port.*;
import org.jetbrains.annotations.NotNull;

public class EventServiceFactory {

    @NotNull
    public static EventService createServiceWith(EventRepository eventRepository,
                                                 PersonRepository personRepository,
                                                 OrganisationRepository organisationRepository,
                                                 EventCertificateRepository eventCertificateRepository) {
        return new EventService(eventRepository, personRepository, organisationRepository, eventCertificateRepository);
    }

    @NotNull
    public static EventService withDefaults() {
        return createServiceWith(new InMemoryEventRepository(),
            new InMemoryPersonRepository(),
            new InMemoryOrganisationRepository(),
            new InMemoryEventCertificateRepository());
    }

}
