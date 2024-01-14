package de.jobst.resulter.domain;

import java.util.Objects;

public record PersonId(Long value) {

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
}
