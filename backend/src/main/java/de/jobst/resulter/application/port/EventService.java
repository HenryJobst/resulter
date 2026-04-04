package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.*;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jmolecules.architecture.hexagonal.PrimaryPort;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@PrimaryPort
public interface EventService {

    Event findOrCreate(Event event);

    Event getById(EventId eventId);

    List<Event> getByIds(Collection<EventId> eventIds);

    Optional<Event> findById(EventId eventId);

    List<Event> findAll();

    List<Event> findAllById(Collection<EventId> eventIds);

    Map<EventId, Event> findAllByIdAsMap(Set<EventId> eventIds);

    @NonNull
    Event updateEvent(
            EventId id,
            @NonNull EventName name,
            @Nullable DateTime startDate,
            @NonNull EventStatus status,
            @NonNull Collection<OrganisationId> organisationIds,
            @Nullable EventCertificateId certificateId,
            Discipline discipline,
            boolean aggregatedScore);

    @Transactional
    void deleteEvent(EventId eventId);

    Event createEvent(
            String eventName,
            ZonedDateTime dateTime,
            Set<OrganisationId> organisationIds,
            String discipline,
            Boolean aggregatedScore);

    Page<Event> findAll(@Nullable String filter, @NonNull Pageable pageable);
}
