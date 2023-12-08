package de.jobst.resulter.domain;

public record Position(Long value) {
    public static Position of(Long value) {
        return new Position(value);
    }
}
