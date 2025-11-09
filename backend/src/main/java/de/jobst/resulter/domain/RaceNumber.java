package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record RaceNumber(Byte value) implements Comparable<RaceNumber> {

    public static RaceNumber of(Byte value) {
        return new RaceNumber(value);
    }

    public int compareTo(RaceNumber raceNumber) {
        return value.compareTo(raceNumber.value);
    }

    public static RaceNumber empty() {
        return of((byte) 1);
    }
}
