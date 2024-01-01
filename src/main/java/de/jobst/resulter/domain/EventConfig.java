package de.jobst.resulter.domain;

import java.util.EnumSet;
import java.util.Objects;

public record EventConfig(EnumSet<ShallowEventLoads> shallowLoads) {

    static public EventConfig of(EnumSet<ShallowEventLoads> shallowLoads) {
        return new EventConfig(shallowLoads);
    }

    static public EventConfig full() {
        return new EventConfig(EnumSet.noneOf(ShallowEventLoads.class));
    }

    static public EventConfig empty() {
        return new EventConfig(EnumSet.allOf(ShallowEventLoads.class));
    }

    public static EventConfig fromEvent(Event event) {
        EnumSet<ShallowEventLoads> shallowLoads = EnumSet.noneOf(ShallowEventLoads.class);

        if (event.getClassResults().isEmpty()) {
            shallowLoads.add(ShallowEventLoads.CLASS_RESULTS);
            shallowLoads.add(ShallowEventLoads.PERSON_RESULTS);
            shallowLoads.add(ShallowEventLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(ShallowEventLoads.SPLIT_TIMES);
        } else if (event.getClassResults()
                .get().value()
                .stream()
                .anyMatch(y -> y.getPersonResults().isEmpty())) {
            shallowLoads.add(ShallowEventLoads.PERSON_RESULTS);
            shallowLoads.add(ShallowEventLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(ShallowEventLoads.SPLIT_TIMES);
        } else if (event.getClassResults()
                .get()
                .value()
                .stream()
                .flatMap(x -> x.getPersonResults().get().value().stream())
                .noneMatch(y -> y.getPersonRaceResults().isLoaded())) {
            shallowLoads.add(ShallowEventLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(ShallowEventLoads.SPLIT_TIMES);
        } else if (Objects.requireNonNull(event.getClassResults())
                .get().value()
                .stream()
                .flatMap(x -> x.getPersonResults()
                        .get().value()
                        .stream()
                        .filter(y -> y.getPersonRaceResults().isLoaded())
                        .flatMap(z -> z.getPersonRaceResults().get().value()
                                .stream())).noneMatch(u -> u.getSplitTimes().isLoaded())) {
            shallowLoads.add(ShallowEventLoads.SPLIT_TIMES);
        }
        if (event.getOrganisations().isEmpty()) {
            shallowLoads.add(ShallowEventLoads.EVENT_ORGANISATIONS);
        }
        return EventConfig.of(shallowLoads);
    }

    public enum ShallowEventLoads {
        CLASS_RESULTS,
        PERSON_RESULTS,
        PERSON_RACE_RESULTS,
        SPLIT_TIMES,
        PERSONS,
        ORGANISATIONS,
        EVENT_ORGANISATIONS
    }
}