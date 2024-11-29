package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventRepository {

    Event save(Event event);

    void deleteEvent(Event event);

    List<Event> findAll();

    Optional<Event> findById(EventId EventId);

    Event findOrCreate(Event event);

    Page<Event> findAll(@Nullable String filter, @NonNull Pageable pageable);

    List<Event> findAllById(Collection<EventId> eventIds);
}
