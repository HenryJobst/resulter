package de.jobst.resulter.domain;

import java.time.ZonedDateTime;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

@ValueObject
public record DateTime(@Nullable ZonedDateTime value) implements Comparable<DateTime> {
    public static DateTime of(@Nullable ZonedDateTime value) {
        return new DateTime(value);
    }

    public static DateTime empty() {
        return DateTime.of(null);
    }

    @Override
    public int compareTo(DateTime o) {
        if (this.value == null && o.value == null) {
            return 0; // Both are empty
        } else if (this.value == null) {
            return -1; // Null is considered less than non-null
        } else if (o.value == null) {
            return 1; // Non-null is greater than null
        }
        return this.value.compareTo(o.value);
    }
}
