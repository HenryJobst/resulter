package de.jobst.resulter.application;

import de.jobst.resulter.application.port.*;
import org.jetbrains.annotations.NotNull;

public class EventServiceFactory {

    @NotNull
    public static EventService createServiceWith(EventRepository eventRepository,
                                                 CupRepository cupRepository,
                                                 ResultListRepository resultListRepository,
                                                 PersonRepository personRepository,
                                                 OrganisationRepository organisationRepository) {
        return new EventService(eventRepository,
            personRepository,
            organisationRepository,
            resultListRepository,
            cupRepository);
    }

    @NotNull
    public static EventService withDefaults() {
        return createServiceWith(new InMemoryEventRepository(),
            new InMemoryCupRepository(),
            new InMemoryResultListRepository(),
            new InMemoryPersonRepository(),
            new InMemoryOrganisationRepository());
    }

}
