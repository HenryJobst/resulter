package de.jobst.resulter.domain;

import java.util.Objects;

public record EventId(Long value) {

    public static EventId of(Long value) {
        if (value != null && value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0 or null.");
        }
        return new EventId(value);
    }

    public static EventId empty() {
        return new EventId(0L);
    }

    public boolean isPersistent() {
        return !Objects.equals(value, empty().value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
