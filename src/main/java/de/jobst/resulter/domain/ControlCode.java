package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

@ValueObject
public record ControlCode(String value) implements Comparable<ControlCode> {
    public static ControlCode of(String value) {
        return new ControlCode(value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }

    @Override
    public int compareTo(@NonNull ControlCode o) {
        if (value == null) {
            return (o.value == null) ? 0 : -1;
        }
        if (o.value == null) {
            return 1;
        }
        return value.compareTo(o.value());
    }
}
