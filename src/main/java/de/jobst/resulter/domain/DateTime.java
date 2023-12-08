package de.jobst.resulter.domain;

import java.time.LocalDateTime;

public record DateTime(LocalDateTime value) {
    public static DateTime of(LocalDateTime value) {
        return new DateTime(value);
    }
}
