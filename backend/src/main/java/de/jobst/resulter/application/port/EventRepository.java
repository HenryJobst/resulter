package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@SecondaryPort
public interface EventRepository {

    @NonNull
    Event save(@NonNull Event event);

    void deleteEvent(Event event);

    List<Event> findAll();

    Optional<Event> findById(EventId EventId);

    Event findOrCreate(Event event);

    Page<Event> findAll(@Nullable String filter, @NonNull Pageable pageable);

    List<Event> findAllById(Collection<EventId> eventIds);
}
