package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import org.springframework.lang.NonNull;

public record MediaFileName(String value) implements Comparable<MediaFileName> {

    public static MediaFileName of(String name) {
        ValueObjectChecks.requireNotEmpty(name);
        return new MediaFileName(name);
    }

    @Override
    public int compareTo(@NonNull MediaFileName o) {
        return value.compareTo(o.value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
