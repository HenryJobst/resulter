package de.jobst.resulter.domain.comparators;

import de.jobst.resulter.domain.Race;

import java.util.Comparator;

public class RaceComparator {

    public static final Comparator<Race> COMPARATOR =
    Comparator.comparing(Race::getRaceNumber)
        .thenComparing(Race::getRaceName, Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(Race::getEventId);
}
