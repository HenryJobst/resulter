package de.jobst.resulter.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;


@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Getter
public class Event {
    @NonNull
    @Setter
    private EventName name;

    @Nullable
    @Setter
    private EventId id;
    @Nullable
    @Setter
    private DateTime startTime;
    @Nullable
    private DateTime endTime;
    @NonNull
    private Optional<ClassResults> classResults = Optional.empty();
    @NonNull
    private Optional<Organisations> organisations = Optional.empty();
    @Nullable
    private EventStatus eventState;

    public Event(@NonNull EventName name) {
        this.name = name;
    }

    public Event(@Nullable EventId id,
                 @NonNull EventName eventName,
                 @Nullable DateTime startTime,
                 @Nullable DateTime endTime,
                 @NonNull Optional<ClassResults> classResults,
                 @NonNull Optional<Organisations> organisations,
                 @Nullable EventStatus eventState) {
        this.id = id;
        this.name = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classResults = classResults;
        this.organisations = organisations;
        this.eventState = eventState;
    }

    public static Event of(String name) {
        return Event.of(name, new ArrayList<>());
    }

    public static Event of(String name, Collection<ClassResult> classResults) {
        return Event.of(EventId.empty().value(), name, classResults);
    }

    public static Event of(long id, String name) {
        return Event.of(id, name, new ArrayList<>());
    }

    public static Event of(long id, String name, Collection<ClassResult> classResults) {
        return Event.of(id, name, null, null, classResults, new ArrayList<>(), null);
    }

    public static Event of(String name, Collection<ClassResult> classResults, Collection<Organisation> organisations) {
        return Event.of(EventId.empty().value(),
                name,
                null,
                null,
                classResults,
                organisations,
                null);
    }

    static public Event of(long id, String eventName, LocalDateTime startTime,
                           LocalDateTime endTime, Collection<ClassResult> classResults,
                           Collection<Organisation> organisations, EventStatus eventState) {
        return new Event(EventId.of(id),
                EventName.of(eventName),
                DateTime.of(startTime),
                DateTime.of(endTime),
                Optional.of(ClassResults.of(classResults)),
                Optional.of(Organisations.of(organisations)),
                eventState);
    }
}
