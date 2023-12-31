package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ShallowLoadProxy;
import de.jobst.resulter.domain.util.ValueObjectChecks;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;


@Getter
public class Event {
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
    private final ShallowLoadProxy<ClassResults> classResults;
    @NonNull
    private ShallowLoadProxy<Organisations> organisations;
    @Nullable
    private final EventStatus eventState;
    @NonNull
    private ShallowLoadProxy<Cups> cups;

    public Event(@NonNull EventId id,
                 @NonNull EventName eventName,
                 @NonNull DateTime startTime,
                 @NonNull DateTime endTime,
                 @NonNull ShallowLoadProxy<ClassResults> classResults,
                 @NonNull ShallowLoadProxy<Organisations> organisations,
                 @NonNull ShallowLoadProxy<Cups> cups,
                 @Nullable EventStatus eventState) {
        this.id = id;
        this.name = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classResults = classResults;
        this.organisations = organisations;
        this.cups = cups;
        this.eventState = eventState;
    }

    public static Event of(@NonNull String name) {
        return Event.of(name, new ArrayList<>());
    }

    public static Event of(@NonNull String name, @Nullable Collection<ClassResult> classResults) {
        return Event.of(EventId.empty().value(), name, classResults);
    }

    public static Event of(long id, @NonNull String name) {
        return Event.of(id, name, new ArrayList<>());
    }

    public static Event of(long id, @NonNull String name, @Nullable Collection<ClassResult> classResults) {
        return Event.of(id, name, null, null, classResults,
                new ArrayList<>(), new ArrayList<>(), null);
    }

    public static Event of(@NonNull String name,
                           @Nullable Collection<ClassResult> classResults,
                           @Nullable Collection<Organisation> organisations,
                           @Nullable Collection<Cup> cups) {
        return Event.of(EventId.empty().value(),
                name,
                null,
                null,
                classResults,
                organisations,
                cups,
                null);
    }

    static public Event of(long id,
                           @NonNull String eventName,
                           @Nullable ZonedDateTime startTime,
                           @Nullable ZonedDateTime endTime,
                           @Nullable Collection<ClassResult> classResults,
                           @Nullable Collection<Organisation> organisations,
                           @Nullable Collection<Cup> cups,
                           @Nullable EventStatus eventState) {
        return new Event(EventId.of(id),
                EventName.of(eventName),
                DateTime.of(startTime),
                DateTime.of(endTime),
                (classResults != null) ? ShallowLoadProxy.of(ClassResults.of(classResults)) : ShallowLoadProxy.empty(),
                (organisations != null) ?
                        ShallowLoadProxy.of(Organisations.of(organisations)) :
                        ShallowLoadProxy.empty(),
                (cups != null) ?
                        ShallowLoadProxy.of(Cups.of(cups)) :
                        ShallowLoadProxy.empty(),
                eventState);
    }

    public void update(EventName eventName, DateTime startTime, Organisations organisations, Cups cups) {
        ValueObjectChecks.requireNotNull(eventName);
        this.name = eventName;
        this.startTime = startTime;
        this.organisations = organisations != null ? ShallowLoadProxy.of(organisations) : ShallowLoadProxy.empty();
        this.cups = cups != null ? ShallowLoadProxy.of(cups) : ShallowLoadProxy.empty();
    }
}
