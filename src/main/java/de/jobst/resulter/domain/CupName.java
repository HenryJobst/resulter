package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;

public record CupName(String value) {

    public static CupName of(String name) {
        ValueObjectChecks.requireNotEmpty(name);
        return new CupName(name);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
