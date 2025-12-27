package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record CourseClimb(Double value) implements Comparable<CourseClimb> {
    public static CourseClimb of(Double courseClimb) {
        return new CourseClimb(courseClimb);
    }

    @Override
    public int compareTo(CourseClimb o) {
        return value.compareTo(o.value);
    }
}
