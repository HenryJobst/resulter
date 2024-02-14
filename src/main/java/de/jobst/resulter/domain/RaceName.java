package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import org.springframework.lang.NonNull;

public record RaceName(String value) implements Comparable<RaceName> {

    public static RaceName of(String name) {
        ValueObjectChecks.requireNotEmpty(name);
        return new RaceName(name);
    }

    @Override
    public int compareTo(@NonNull RaceName o) {
        return value.compareTo(o.value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
