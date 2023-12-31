package de.jobst.resulter.domain;

import java.util.EnumSet;

public record CupConfig(EnumSet<ShallowCupLoads> shallowLoads, EventConfig eventConfig) {

    static public CupConfig of(EnumSet<ShallowCupLoads> shallowLoads, EventConfig eventConfig) {
        return new CupConfig(shallowLoads, eventConfig);
    }

    static public CupConfig full() {
        return new CupConfig(EnumSet.noneOf(ShallowCupLoads.class), EventConfig.full());
    }

    static public CupConfig empty() {
        return new CupConfig(EnumSet.allOf(ShallowCupLoads.class), EventConfig.empty());
    }

    public static CupConfig fromCup(Cup cup) {
        EnumSet<ShallowCupLoads> shallowLoads = EnumSet.noneOf(ShallowCupLoads.class);

        if (cup.getEvents().isEmpty()) {
            shallowLoads.add(ShallowCupLoads.EVENTS);
        }
        return CupConfig.of(shallowLoads, EventConfig.empty());
    }

    public enum ShallowCupLoads {
        EVENTS,
    }
}