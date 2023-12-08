package de.jobst.resulter.application;

import de.jobst.resulter.adapter.out.jpa.inmem.InMemoryEventRepository;
import de.jobst.resulter.application.port.EventRepository;
import org.jetbrains.annotations.NotNull;

public class EventServiceFactory {

    @NotNull
    public static EventService createServiceWith(EventRepository eventRepository) {
        return new EventService(eventRepository);
    }

    @NotNull
    public static EventService withDefaults() {
        return createServiceWith(new InMemoryEventRepository());
    }

}
