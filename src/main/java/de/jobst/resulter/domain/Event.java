package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;


@Getter
public class Event implements Comparable<Event> {

    @NonNull
    @Setter
    private EventId id;
    @NonNull
    private EventName name;
    @Nullable
    private DateTime startTime;
    @NonNull
    private final DateTime endTime;
    @NonNull
    private EventStatus eventState;
    @NonNull
    private Collection<Organisation> organisations;

    public Event(@NonNull EventId id,
                 @NonNull EventName eventName,
                 @NonNull DateTime startTime,
                 @NonNull DateTime endTime,
                 @NonNull Collection<Organisation> organisations,
                 @NonNull EventStatus eventState) {
        this.id = id;
        this.name = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.organisations = organisations;
        this.eventState = eventState;
    }

    public static Event of(@NonNull String name) {
        return Event.of(EventId.empty().value(), name);
    }

    public static Event of(Long id, @NonNull String name) {
        return Event.of(id, name, null, null, new HashSet<>(), EventStatus.getDefault());
    }

    public static Event of(@NonNull String name, @NonNull Collection<Organisation> organisations) {
        return Event.of(EventId.empty().value(), name, null, null, organisations, EventStatus.getDefault());
    }

    static public Event of(Long id,
                           @NonNull String eventName,
                           @Nullable ZonedDateTime startTime,
                           @Nullable ZonedDateTime endTime,
                           @NonNull Collection<Organisation> organisations,
                           @NonNull EventStatus eventState) {
        return new Event(EventId.of(id),
            EventName.of(eventName),
            DateTime.of(startTime),
            DateTime.of(endTime),
            organisations,
            eventState);
    }


    public void update(@NonNull EventName eventName,
                       @Nullable DateTime startTime,
                       @NonNull EventStatus status,
                       @NonNull Collection<Organisation> organisations) {
        ValueObjectChecks.requireNotNull(eventName);
        this.name = eventName;
        this.startTime = startTime;
        this.eventState = status;
        this.organisations = organisations;
    }

    @Override
    public int compareTo(@NonNull Event o) {
        return name.compareTo(o.name);
    }

}
