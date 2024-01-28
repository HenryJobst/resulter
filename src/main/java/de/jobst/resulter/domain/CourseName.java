package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

public record CourseName(String value) implements Comparable<CourseName> {
    public static CourseName of(String courseName) {
        return new CourseName(courseName);
    }

    @Override
    public int compareTo(@NonNull CourseName o) {
        return value.compareTo(o.value);
    }
}
