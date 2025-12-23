package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;

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
    public int compareTo(ControlCode o) {
        return value.compareTo(o.value());
    }
}
