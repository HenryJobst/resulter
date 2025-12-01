package de.jobst.resulter.domain;

import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

@ValueObject
public record EventCertificateId(Long value) implements Comparable<EventCertificateId> {

    public static EventCertificateId of(@Nullable Long value) {
        if (value == null || value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0 or null.");
        }
        return new EventCertificateId(value);
    }

    public static EventCertificateId empty() {
        return new EventCertificateId(0L);
    }

    @Override
    public int compareTo(EventCertificateId o) {
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
