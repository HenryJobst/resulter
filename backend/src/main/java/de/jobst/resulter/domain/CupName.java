package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

@ValueObject
public record CupName(String value) implements Comparable<CupName> {

    public static CupName of(String name) {
        ValueObjectChecks.requireNotEmpty(name);
        return new CupName(name);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }

    @Override
    public int compareTo(@NonNull CupName o) {
        return value.compareTo(o.value);
    }
}
