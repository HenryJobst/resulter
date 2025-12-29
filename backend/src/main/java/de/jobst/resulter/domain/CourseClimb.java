package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@ValueObject
public record CourseClimb(@Nullable Double value) implements Comparable<CourseClimb> {
    public static CourseClimb of(@Nullable Double courseClimb) {
        return new CourseClimb(courseClimb);
    }

    @Override
    public int compareTo(CourseClimb o) {
        return Objects.compare(value, o.value, Double::compareTo);
    }
}
