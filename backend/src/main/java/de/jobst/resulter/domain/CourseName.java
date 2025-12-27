package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record CourseName(String value) implements Comparable<CourseName> {
    public static CourseName of(String courseName) {
        return new CourseName(courseName);
    }

    @Override
    public int compareTo(CourseName o) {
        return value.compareTo(o.value);
    }
}
