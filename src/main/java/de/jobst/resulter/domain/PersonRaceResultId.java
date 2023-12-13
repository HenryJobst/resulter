package de.jobst.resulter.domain;

public record PersonRaceResultId(long value) {

    public static PersonRaceResultId of(long value) {
        return new PersonRaceResultId(value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
