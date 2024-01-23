package de.jobst.resulter.domain;

import java.time.ZonedDateTime;

public record BirthDate(ZonedDateTime value) {

    public static BirthDate of(ZonedDateTime birthDate) {
        return new BirthDate(birthDate);
    }
}
