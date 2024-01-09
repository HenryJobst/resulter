package de.jobst.resulter.domain;

public record RaceNumber(Long value) implements Comparable<RaceNumber> {
    public static RaceNumber of(Long value) {
        return new RaceNumber(value);
    }

    public int compareTo(RaceNumber raceNumber) {
        return value.compareTo(raceNumber.value);
    }
}
