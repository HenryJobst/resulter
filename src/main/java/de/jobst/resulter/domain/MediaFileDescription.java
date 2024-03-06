package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import org.springframework.lang.NonNull;

public record MediaFileDescription(String value) implements Comparable<MediaFileDescription> {

    public static MediaFileDescription of(String name) {
        ValueObjectChecks.requireNotEmpty(name);
        return new MediaFileDescription(name);
    }

    @Override
    public int compareTo(@NonNull MediaFileDescription o) {
        return value.compareTo(o.value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
