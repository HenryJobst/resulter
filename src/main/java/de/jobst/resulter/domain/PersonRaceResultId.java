package de.jobst.resulter.domain;

public record PersonRaceResultId(long value) {

    public static PersonRaceResultId of(long value) {
        if (value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0.");
        }
        return new PersonRaceResultId(value);
    }

    public static PersonRaceResultId empty() {
        return new PersonRaceResultId(0L);
    }

    public boolean isPersistent() {
        return value != empty().value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
