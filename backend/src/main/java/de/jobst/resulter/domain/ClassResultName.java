package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

@ValueObject
public record ClassResultName(@Nullable String value) {
    public static ClassResultName of(String name) {
        return new ClassResultName(name);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }

    public int compareTo(ClassResultName o) {
        if (value == null) {
            return (o.value == null) ? 0 : -1;
        }
        if (o.value == null) {
            return 1;
        }
        return value.compareTo(o.value);
    }
}
