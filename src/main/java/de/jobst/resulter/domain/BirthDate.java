package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

import java.time.LocalDate;

@ValueObject
public record BirthDate(LocalDate value) implements Comparable<BirthDate> {

    public static BirthDate of(LocalDate birthDate) {
        return new BirthDate(birthDate);
    }

    @Override
    public int compareTo(@NonNull BirthDate o) {
        return value != null && o.value != null ? value.compareTo(o.value) : value == null ? -1 : 1;
    }
}
