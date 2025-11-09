package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

@ValueObject
public record PunchTime(Double value) implements Comparable<PunchTime> {
    public static PunchTime of(Double value) {
        return new PunchTime(value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }

    @Override
    public int compareTo(@NonNull PunchTime o) {
        if (value == null) {
            return (o.value == null) ? 0 : -1;
        }
        if (o.value == null) {
            return 1;
        }
        return value.compareTo(o.value);
    }
}
