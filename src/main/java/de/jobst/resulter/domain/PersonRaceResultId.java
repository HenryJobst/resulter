package de.jobst.resulter.domain;

public record PersonRaceResultId(long value) {

    public static PersonRaceResultId of(long value) {
        return new PersonRaceResultId(value);
    }

    public static PersonRaceResultId empty() {
        return new PersonRaceResultId(0L);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
