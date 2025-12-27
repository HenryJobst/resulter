package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

@ValueObject
public record EventCertificateLayoutDescription(String value) implements Comparable<EventCertificateLayoutDescription> {

    public static EventCertificateLayoutDescription of(String description) {
        ValueObjectChecks.requireNotEmpty(description);
        return new EventCertificateLayoutDescription(description);
    }

    @Override
    public int compareTo(@NonNull EventCertificateLayoutDescription o) {
        return value.compareTo(o.value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
