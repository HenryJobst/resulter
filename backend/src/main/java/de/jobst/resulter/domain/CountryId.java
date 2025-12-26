package de.jobst.resulter.domain;

import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record CountryId(Long value) implements Comparable<CountryId> {

    public static CountryId of(Long value) {
        if (value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0 or null.");
        }
        return new CountryId(value);
    }

    public static CountryId empty() {
        return new CountryId(0L);
    }

    public boolean isPersistent() {
        return !Objects.equals(value, empty().value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }

    @Override
    public int compareTo(CountryId o) {
        return Long.compare(value, o.value);
    }
}
