package de.jobst.resulter.domain;

public record RaceNumber(Long value) {
    public static RaceNumber of(Long value) {
        return new RaceNumber(value);
    }
}
