package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

import java.util.Objects;

@ValueObject
public record EventId(Long value) implements Comparable<EventId> {

    public static EventId of(Long value) {
        if (value != null && value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0 or null.");
        }
        return new EventId(value);
    }

    public static EventId empty() {
        return new EventId(0L);
    }

    @Override
    public int compareTo(@NonNull EventId o) {
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
