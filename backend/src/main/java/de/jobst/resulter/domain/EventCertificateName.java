package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

@ValueObject
public record EventCertificateName(String value) implements Comparable<EventCertificateName> {

    public static EventCertificateName of(String name) {
        ValueObjectChecks.requireNotEmpty(name);
        return new EventCertificateName(name);
    }

    @Override
    public int compareTo(@NonNull EventCertificateName o) {
        return value.compareTo(o.value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
