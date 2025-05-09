package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

@ValueObject
public record CourseClimb(Double value) implements Comparable<CourseClimb> {
    public static CourseClimb of(Double courseClimb) {
        return new CourseClimb(courseClimb);
    }

    @Override
    public int compareTo(@NonNull CourseClimb o) {
        return value.compareTo(o.value);
    }
}
