package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

@ValueObject
public record CourseLength(Double value) implements Comparable<CourseLength> {
    public static CourseLength of(Double courseLength) {
        return new CourseLength(courseLength);
    }

    @Override
    public int compareTo(@NonNull CourseLength o) {
        return value.compareTo(o.value);
    }
}
