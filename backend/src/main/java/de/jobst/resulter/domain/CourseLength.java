package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Objects;

@ValueObject
public record CourseLength(@Nullable Double value) implements Comparable<CourseLength> {
    public static CourseLength of(@Nullable Double courseLength) {
        return new CourseLength(courseLength);
    }

    @Override
    public int compareTo(CourseLength o) {
        return Objects.compare(value, o.value, Comparator.nullsLast(Comparator.naturalOrder()));
    }
}
