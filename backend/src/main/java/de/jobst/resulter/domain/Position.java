package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@ValueObject
public record Position(@Nullable Long value) implements Comparable<Position> {
    public static Position of(@Nullable Long value) {
        return new Position(value);
    }

    @Override
    public int compareTo(Position o) {
        return Objects.compare(this, o, Position::compareTo);
    }
}
