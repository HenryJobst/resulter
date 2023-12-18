package de.jobst.resulter.domain;

import java.util.EnumSet;
import java.util.Objects;

public record EventConfig(EnumSet<ShallowLoads> shallowLoads) {
    static public EventConfig of(EnumSet<ShallowLoads> shallowLoads) {
        return new EventConfig(shallowLoads);
    }

    static public EventConfig full() {
        return new EventConfig(EnumSet.of(ShallowLoads.PERSON_RACE_RESULTS, ShallowLoads.SPLIT_TIMES));
    }

    public static EventConfig fromEvent(Event event) {
        EnumSet<ShallowLoads> shallowLoads = EnumSet.noneOf(ShallowLoads.class);

        if (Objects.requireNonNull(event.getClassResults())
                .value()
                .stream()
                .flatMap(x -> x.personResults().value().stream()).noneMatch(y -> y.personRaceResults().isPresent())) {
            shallowLoads.add(ShallowLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(ShallowLoads.SPLIT_TIMES);
        } else if (Objects.requireNonNull(event.getClassResults())
                .value()
                .stream()
                .flatMap(x -> x.personResults()
                        .value()
                        .stream()
                        .filter(y -> y.personRaceResults().isPresent())
                        .flatMap(z -> z.personRaceResults().get().value()
                                .stream())).noneMatch(u -> u.splitTimes().isPresent())) {
            shallowLoads.add(ShallowLoads.SPLIT_TIMES);
        }
        return EventConfig.of(shallowLoads);
    }

    public enum ShallowLoads {PERSON_RACE_RESULTS, SPLIT_TIMES}
}