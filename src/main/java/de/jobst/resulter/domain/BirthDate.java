package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

import java.time.LocalDate;

public record BirthDate(LocalDate value) implements Comparable<BirthDate> {

    public static BirthDate of(LocalDate birthDate) {
        return new BirthDate(birthDate);
    }

    @Override
    public int compareTo(@NonNull BirthDate o) {
        return value.compareTo(o.value);
    }
}
