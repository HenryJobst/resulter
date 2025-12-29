package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@ValueObject
public record ControlCode(@Nullable String value) implements Comparable<ControlCode> {
    public static ControlCode of(@Nullable String value) {
        return new ControlCode(value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }

    @Override
    public int compareTo(ControlCode o) {
        return Objects.compare(value, o.value, String::compareTo);
    }
}
