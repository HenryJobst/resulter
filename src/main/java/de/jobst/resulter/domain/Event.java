package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;


@Getter
public class Event implements Comparable<Event> {

    @NonNull
    @Setter
    private EventId id;
    @NonNull
    private EventName name;
    @NonNull
    private DateTime startTime;
    @NonNull
    private final DateTime endTime;
    @NonNull
    private EventStatus eventState;
    @NonNull
    private Set<OrganisationId> organisationIds;

    public Event(@NonNull EventId id,
                 @NonNull EventName eventName,
                 @NonNull DateTime startTime,
                 @NonNull DateTime endTime,
                 @NonNull Set<OrganisationId> organisationIds,
                 @NonNull EventStatus eventState) {
        this.id = id;
        this.name = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.organisationIds = organisationIds;
        this.eventState = eventState;
    }

    public static Event of(@NonNull String name) {
        return Event.of(EventId.empty().value(), name);
    }

    public static Event of(Long id, @NonNull String name) {
        return Event.of(id, name, null, null, new HashSet<>(), EventStatus.getDefault());
    }

    public static Event of(@NonNull String name, @Nullable Set<OrganisationId> organisations) {
        return Event.of(EventId.empty().value(), name, null, null, organisations, EventStatus.getDefault());
    }

    static public Event of(Long id,
                           @NonNull String eventName,
                           @Nullable ZonedDateTime startTime,
                           @Nullable ZonedDateTime endTime,
                           @Nullable Set<OrganisationId> organisationIds,
                           @NonNull EventStatus eventState) {
        return new Event(EventId.of(id),
            EventName.of(eventName),
            DateTime.of(startTime),
            DateTime.of(endTime),
            (organisationIds != null) ? organisationIds : new HashSet<>(),
            eventState);
    }


    public void update(EventName eventName,
                       DateTime startTime,
                       EventStatus status,
                       Set<OrganisationId> organisationIds) {
        ValueObjectChecks.requireNotNull(eventName);
        this.name = eventName;
        this.startTime = startTime;
        this.eventState = status;
        this.organisationIds = organisationIds != null ? organisationIds : new HashSet<>();
    }

    @Override
    public int compareTo(@NonNull Event o) {
        return name.compareTo(o.name);
    }

}
