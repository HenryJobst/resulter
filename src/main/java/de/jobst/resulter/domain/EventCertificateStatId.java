package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

import java.util.Objects;

public record EventCertificateStatId(Long value) implements Comparable<EventCertificateStatId> {

    public static EventCertificateStatId of(Long value) {
        if (value != null && value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0 or null.");
        }
        return new EventCertificateStatId(value);
    }

    public static EventCertificateStatId empty() {
        return new EventCertificateStatId(0L);
    }

    @Override
    public int compareTo(@NonNull EventCertificateStatId o) {
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
