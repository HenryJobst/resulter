package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ShallowLoadProxy;
import de.jobst.resulter.domain.util.ValueObjectChecks;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.Collection;


@Getter
public class Event {
    @Nullable
    @Setter
    private EventId id;
    @NonNull
    private EventName name;
    @NonNull
    private DateTime startTime = DateTime.empty();
    @NonNull
    private DateTime endTime = DateTime.empty();
    @NonNull
    private ShallowLoadProxy<ClassResults> classResults = ShallowLoadProxy.empty();
    @NonNull
    private ShallowLoadProxy<Organisations> organisations = ShallowLoadProxy.empty();
    @Nullable
    private EventStatus eventState;

    public Event(@NonNull EventName name) {
        this.name = name;
    }

    public Event(@Nullable EventId id,
                 @NonNull EventName eventName,
                 @NonNull DateTime startTime,
                 @NonNull DateTime endTime,
                 @NonNull ShallowLoadProxy<ClassResults> classResults,
                 @NonNull ShallowLoadProxy<Organisations> organisations,
                 @Nullable EventStatus eventState) {
        this.id = id;
        this.name = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classResults = classResults;
        this.organisations = organisations;
        this.eventState = eventState;
    }

    public static Event of(@NonNull String name) {
        return Event.of(name, null);
    }

    public static Event of(@NonNull String name, @Nullable Collection<ClassResult> classResults) {
        return Event.of(EventId.empty().value(), name, classResults);
    }

    public static Event of(long id, @NonNull String name) {
        return Event.of(id, name, null);
    }

    public static Event of(long id, @NonNull String name, @Nullable Collection<ClassResult> classResults) {
        return Event.of(id, name, null, null, classResults,
                null, null);
    }

    public static Event of(@NonNull String name,
                           @Nullable Collection<ClassResult> classResults,
                           @Nullable Collection<Organisation> organisations) {
        return Event.of(EventId.empty().value(),
                name,
                null,
                null,
                classResults,
                organisations,
                null);
    }

    static public Event of(long id,
                           @NonNull String eventName,
                           @Nullable ZonedDateTime startTime,
                           @Nullable ZonedDateTime endTime,
                           @Nullable Collection<ClassResult> classResults,
                           @Nullable Collection<Organisation> organisations,
                           @Nullable EventStatus eventState) {
        return new Event(EventId.of(id),
                EventName.of(eventName),
                DateTime.of(startTime),
                DateTime.of(endTime),
                (classResults != null) ? ShallowLoadProxy.of(ClassResults.of(classResults)) : ShallowLoadProxy.empty(),
                (organisations != null) ?
                        ShallowLoadProxy.of(Organisations.of(organisations)) :
                        ShallowLoadProxy.empty(),
                eventState);
    }

    public void update(EventName eventName, DateTime startTime) {
        ValueObjectChecks.requireNotNull(eventName);
        this.name = eventName;
        this.startTime = startTime;
    }
}
