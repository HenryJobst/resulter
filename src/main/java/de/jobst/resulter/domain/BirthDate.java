package de.jobst.resulter.domain;

import java.time.LocalDate;

public record BirthDate(LocalDate value) {
    public static BirthDate of(LocalDate birthDate) {
        return new BirthDate(birthDate);
    }
}
