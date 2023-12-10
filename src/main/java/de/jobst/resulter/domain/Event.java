package de.jobst.resulter.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;


@Getter
public class Event {
    @NonNull final private EventName name;

    @Nullable
    @Setter
    private EventId id;
    @Nullable
    private DateTime startTime;
    @Nullable
    private DateTime endTime;
    @Nullable
    private ClassResults classResults;
    @Nullable
    private Organisations organisations;

    public Event(@NonNull EventName name) {
        this.name = name;
    }

    public Event(@Nullable EventId eventId, @NonNull EventName eventName, @Nullable DateTime startTime,
                 @Nullable DateTime endTime, @Nullable ClassResults classResults,
                 @Nullable Organisations organisations) {
        this.id = eventId;
        this.name = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classResults = classResults;
        this.organisations = organisations;
    }

    public Event(EventId eventId, EventName eventName, ClassResults classResults) {
        this(eventId, eventName, null, null, classResults, null);
    }

    public static Event of(String name) {
        return new Event(EventId.of(0L), EventName.of(name), ClassResults.of(new ArrayList<>()));
    }

    public static Event of(String name, Collection<ClassResult> classResults) {
        return new Event(EventId.of(0L), EventName.of(name), ClassResults.of(classResults));
    }

    public static Event of(long id, String name) {
        return new Event(EventId.of(id), EventName.of(name), ClassResults.of(new ArrayList<>()));
    }

    public static Event of(long id, String name, Collection<ClassResult> classResults) {
        return new Event(EventId.of(id), EventName.of(name), ClassResults.of(classResults));
    }

    public static Event of(String name, Collection<ClassResult> classResults, Collection<Organisation> organisations) {
        return new Event(EventId.of(0L),
                EventName.of(name),
                null,
                null,
                ClassResults.of(classResults),
                Organisations.of(organisations));
    }
}
