package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;

import java.util.List;
import java.util.Optional;

public interface EventRepository {
    Event save(Event event);

    List<Event> findAll();

    Optional<Event> findById(EventId EventId);

    Event findOrCreate(Event event);
}
