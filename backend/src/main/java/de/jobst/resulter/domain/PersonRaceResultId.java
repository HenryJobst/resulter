package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record PersonRaceResultId(long value) implements Comparable<PersonRaceResultId> {

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

    public int compareTo(PersonRaceResultId o) {
        return Long.compare(value, o.value);
    }
}
