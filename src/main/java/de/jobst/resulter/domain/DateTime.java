package de.jobst.resulter.domain;

import java.time.ZonedDateTime;

public record DateTime(ZonedDateTime value) {
    public static DateTime of(ZonedDateTime value) {
        return new DateTime(value);
    }
}
