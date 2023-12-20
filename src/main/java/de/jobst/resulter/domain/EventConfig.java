package de.jobst.resulter.domain;

import java.util.EnumSet;
import java.util.Objects;

public record EventConfig(EnumSet<ShallowLoads> shallowLoads) {
    static public EventConfig of(EnumSet<ShallowLoads> shallowLoads) {
        return new EventConfig(shallowLoads);
    }

    static public EventConfig full() {
        return new EventConfig(EnumSet.noneOf(ShallowLoads.class));
    }

    public static EventConfig fromEvent(Event event) {
        EnumSet<ShallowLoads> shallowLoads = EnumSet.noneOf(ShallowLoads.class);

        if (event.getClassResults().isEmpty()) {
            shallowLoads.add(ShallowLoads.CLASS_RESULTS);
            shallowLoads.add(ShallowLoads.PERSON_RESULTS);
            shallowLoads.add(ShallowLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(ShallowLoads.SPLIT_TIMES);
        } else if (event.getClassResults()
                .get().value()
                .stream()
                .noneMatch(y -> y.personResults().isPresent())) {
            shallowLoads.add(ShallowLoads.PERSON_RESULTS);
            shallowLoads.add(ShallowLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(ShallowLoads.SPLIT_TIMES);
        } else if (event.getClassResults()
                .get()
                .value()
                .stream()
                .flatMap(x -> x.personResults().get().value().stream())
                .noneMatch(y -> y.personRaceResults().isPresent())) {
            shallowLoads.add(ShallowLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(ShallowLoads.SPLIT_TIMES);
        } else if (Objects.requireNonNull(event.getClassResults())
                .get().value()
                .stream()
                .flatMap(x -> x.personResults()
                        .get().value()
                        .stream()
                        .filter(y -> y.personRaceResults().isPresent())
                        .flatMap(z -> z.personRaceResults().get().value()
                                .stream())).noneMatch(u -> u.splitTimes().isPresent())) {
            shallowLoads.add(ShallowLoads.SPLIT_TIMES);
        }
        if (event.getOrganisations().isEmpty()) {
            shallowLoads.add(ShallowLoads.EVENT_ORGANISATIONS);
        }
        return EventConfig.of(shallowLoads);
    }

    public enum ShallowLoads {
        CLASS_RESULTS,
        PERSON_RESULTS,
        PERSON_RACE_RESULTS,
        SPLIT_TIMES,
        PERSONS,
        ORGANISATIONS,
        EVENT_ORGANISATIONS
    }
}