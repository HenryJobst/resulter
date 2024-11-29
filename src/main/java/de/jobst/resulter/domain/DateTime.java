package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

import java.time.ZonedDateTime;

public record DateTime(ZonedDateTime value) implements Comparable<DateTime> {
    public static DateTime of(ZonedDateTime value) {
        return new DateTime(value);
    }

    public static DateTime empty() {
        return DateTime.of(null);
    }

    @Override
    public int compareTo(@NonNull DateTime o) {
        return this.value.compareTo(o.value);
    }
}
