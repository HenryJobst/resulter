package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Event;

public interface EventRepository {
    Event findOrCreate(Event event);
}
