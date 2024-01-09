package de.jobst.resulter.domain;

import de.jobst.resulter.domain.scoring.*;
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
    private final ShallowLoadProxy<ClassResults> classResults;
    @NonNull
    private ShallowLoadProxy<Organisations> organisations;
    @Nullable
    private final EventStatus eventState;

    public Event(@NonNull EventId id,
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
                new ArrayList<>(), null);
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

    public void update(EventName eventName, DateTime startTime, Organisations organisations) {
        ValueObjectChecks.requireNotNull(eventName);
        this.name = eventName;
        this.startTime = startTime;
        this.organisations = organisations != null ? ShallowLoadProxy.of(organisations) : ShallowLoadProxy.empty();
    }

    @Override
    public int compareTo(@NonNull Event o) {
        return name.compareTo(o.name);
    }

    public void calculate(Cup cup) {

        if (invalid(cup)) {
            return;
        }

        CupTypeCalculationStrategy cupTypeCalculationStrategy = null;
        switch (cup.getType()) {
            case CupType.NOR -> cupTypeCalculationStrategy = new NORCalculationStrategy();
            case CupType.KRISTALL -> cupTypeCalculationStrategy = new KristallCalculationStrategy();
            case CupType.NEBEL -> cupTypeCalculationStrategy = new NebelCalculationStrategy();
            case CupType.ADD -> cupTypeCalculationStrategy = new AddCalculationStrategy();
        }

        if (cupTypeCalculationStrategy != null) {
            calculate(cupTypeCalculationStrategy);
        }
    }

    private boolean invalid(Cup cup) {
        // event is not in given cup
        return cup.getEvents().get().value().stream().filter(it -> it.getId().equals(this.id)).findAny().isEmpty();
    }

    private void calculate(CupTypeCalculationStrategy cupTypeCalculationStrategy) {
        getClassResults().get().value().stream().filter(cupTypeCalculationStrategy::valid).forEach(
                it -> it.calculate(cupTypeCalculationStrategy));
    }
}
