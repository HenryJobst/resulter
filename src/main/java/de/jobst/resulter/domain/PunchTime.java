package de.jobst.resulter.domain;

public record PunchTime(Double value) {
    public static PunchTime of(Double value) {
        return new PunchTime(value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
