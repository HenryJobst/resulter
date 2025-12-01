package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

@ValueObject
public record PunchTime(@Nullable Double value) implements Comparable<PunchTime> {
    public static PunchTime of(@Nullable Double value) {
        return new PunchTime(value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }

    @Override
    public int compareTo(PunchTime o) {
        if (value == null) {
            return (o.value == null) ? 0 : -1;
        }
        if (o.value == null) {
            return 1;
        }
        return value.compareTo(o.value);
    }
}
