package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

import java.util.Objects;

public record PersonId(Long value) implements Comparable<PersonId> {

    public static PersonId of(Long value) {
        if (value != null && value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0 or null.");
        }
        return new PersonId(value);
    }

    public static PersonId empty() {
        return new PersonId(0L);
    }

    public boolean isPersistent() {
        return !Objects.equals(value, empty().value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }

    @Override
    public int compareTo(@NonNull PersonId o) {
        return value.compareTo(o.value);
    }
}
