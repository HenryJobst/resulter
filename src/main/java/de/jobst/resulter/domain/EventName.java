package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;

public record EventName(String value) {

    public static EventName of(String name) {
        ValueObjectChecks.requireNotEmpty(name);
        return new EventName(name);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
