package de.jobst.resulter.domain;

import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.domain.scoring.*;
import de.jobst.resulter.domain.util.ValueObjectChecks;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
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
    @Nullable
    private final EventStatus eventState;
    @NonNull
    private Collection<OrganisationId> organisationIds;
    @NonNull
    private Collection<ResultListId> resultListIds;

    public Event(@NonNull EventId id,
                 @NonNull EventName eventName,
                 @NonNull DateTime startTime,
                 @NonNull DateTime endTime,
                 @NonNull Collection<ResultListId> resultListIds,
                 @NonNull Collection<OrganisationId> organisationIds,
                 @Nullable EventStatus eventState) {
        this.id = id;
        this.name = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.resultListIds = resultListIds;
        this.organisationIds = organisationIds;
        this.eventState = eventState;
    }

    public static Event of(@NonNull String name) {
        return Event.of(name, new ArrayList<>());
    }

    public static Event of(@NonNull String name, @Nullable Collection<ResultListId> resultListIds) {
        return Event.of(EventId.empty().value(), name, resultListIds);
    }

    public static Event of(long id, @NonNull String name) {
        return Event.of(id, name, new ArrayList<>());
    }

    public static Event of(long id, @NonNull String name, @Nullable Collection<ResultListId> resultListIds) {
        return Event.of(id, name, null, null, resultListIds, new ArrayList<>(), null);
    }

    public static Event of(@NonNull String name,
                           @Nullable Collection<ResultListId> resultListIds,
                           @Nullable Collection<OrganisationId> organisations) {
        return Event.of(EventId.empty().value(), name, null, null, resultListIds, organisations, null);
    }

    static public Event of(long id,
                           @NonNull String eventName,
                           @Nullable ZonedDateTime startTime,
                           @Nullable ZonedDateTime endTime,
                           @Nullable Collection<ResultListId> resultListIds,
                           @Nullable Collection<OrganisationId> organisationIds,
                           @Nullable EventStatus eventState) {
        return new Event(EventId.of(id),
            EventName.of(eventName),
            DateTime.of(startTime),
            DateTime.of(endTime),
            (resultListIds != null) ? resultListIds : new ArrayList<>(),
            (organisationIds != null) ? organisationIds : new ArrayList<>(),
            eventState);
    }

    @NonNull
    public Set<OrganisationId> getReferencedOrganisationIds() {
        /*
        return getResultListIds()
            .stream()
            .flatMap(it -> it.personResults().value().stream())
            .map(PersonResult::organisationId)
            .collect(Collectors.toSet());*/
        return new HashSet<>();
    }

    public void update(EventName eventName, DateTime startTime, Collection<OrganisationId> organisationIds) {
        ValueObjectChecks.requireNotNull(eventName);
        this.name = eventName;
        this.startTime = startTime;
        this.organisationIds = organisationIds != null ? organisationIds : new ArrayList<>();
    }

    @Override
    public int compareTo(@NonNull Event o) {
        return name.compareTo(o.name);
    }

    public void calculate(Cup cup, OrganisationRepository organisationRepository) {

        if (invalid(cup)) {
            return;
        }

        CupTypeCalculationStrategy cupTypeCalculationStrategy = null;
        switch (cup.getType()) {
            case CupType.NOR -> cupTypeCalculationStrategy = new NORCalculationStrategy(organisationRepository);
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
        return cup.getEventIds().stream().filter(it -> it.equals(this.id)).findAny().isEmpty();
    }

    private void calculate(CupTypeCalculationStrategy cupTypeCalculationStrategy) {
        /*
        getClassResults().value()
            .stream()
            .filter(cupTypeCalculationStrategy::valid)
            .forEach(it -> it.calculate(cupTypeCalculationStrategy));
         */
    }
}
