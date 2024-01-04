package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import org.springframework.lang.NonNull;

public record EventName(String value) implements Comparable<EventName> {

    public static EventName of(String name) {
        ValueObjectChecks.requireNotEmpty(name);
        return new EventName(name);
    }

    @Override
    public int compareTo(@NonNull EventName o) {
        return value.compareTo(o.value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
