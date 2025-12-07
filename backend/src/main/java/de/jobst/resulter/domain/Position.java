package de.jobst.resulter.domain;

import de.jobst.resulter.domain.comparators.PositionComparator;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

@ValueObject
public record Position(@Nullable Long value) implements Comparable<Position> {
    public static Position of(@Nullable Long value) {
        return new Position(value);
    }

    @Override
    public int compareTo(Position o) {
        return PositionComparator.COMPARATOR.compare(this, o);
    }
}
