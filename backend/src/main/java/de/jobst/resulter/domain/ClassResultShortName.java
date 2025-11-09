package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

@ValueObject
public record ClassResultShortName(String value) implements Comparable<ClassResultShortName> {

    public static ClassResultShortName of(String name) {
        return new ClassResultShortName(name);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }

    @Override
    public int compareTo(@NonNull ClassResultShortName o) {
        return value.compareTo(o.value);
    }
}
