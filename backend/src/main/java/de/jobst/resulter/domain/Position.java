package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

@ValueObject
public record Position(Long value) implements Comparable<Position> {
    public static Position of(Long value) {
        return new Position(value);
    }

    @Override
    public int compareTo(@NonNull Position o) {
        return this.value.compareTo(o.value);
    }
}
