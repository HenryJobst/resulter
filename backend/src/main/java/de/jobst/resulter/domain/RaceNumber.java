package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

@ValueObject
public record RaceNumber(@Nullable Byte value) implements Comparable<RaceNumber> {

    public static RaceNumber of(@Nullable Byte value) {
        return new RaceNumber(value);
    }

    public int compareTo(RaceNumber o) {
        if (value == null) {
            return (o.value == null) ? 0 : -1;
        }
        if (o.value == null) {
            return 1;
        }
        return value.compareTo(o.value);
    }

    public static RaceNumber empty() {
        return of((byte) 1);
    }
}
