package de.jobst.resulter.domain;

import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record RaceId(Long value) implements Comparable<RaceId> {

    public static RaceId of(Long value) {
        if (value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0 or null.");
        }
        return new RaceId(value);
    }

    public static RaceId empty() {
        return new RaceId(0L);
    }

    @Override
    public int compareTo(RaceId o) {
        return value.compareTo(o.value);
    }

    public boolean isPersistent() {
        return !Objects.equals(value, empty().value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
