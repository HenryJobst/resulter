package de.jobst.resulter.domain;

import java.time.LocalDate;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

@ValueObject
public record BirthDate(@Nullable LocalDate value) implements Comparable<BirthDate> {

    public static BirthDate of(@Nullable LocalDate birthDate) {
        return new BirthDate(birthDate);
    }

    @Override
    public int compareTo(@Nullable BirthDate o) {
        return value != null && o != null && o.value != null ? value.compareTo(o.value) : value == null ? -1 : 1;
    }
}
