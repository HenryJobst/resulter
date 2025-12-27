package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record MediaFileName(String value) implements Comparable<MediaFileName> {

    public static MediaFileName of(String name) {
        ValueObjectChecks.requireNotEmpty(name);
        return new MediaFileName(name);
    }

    @Override
    public int compareTo(MediaFileName o) {
        return value.compareTo(o.value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
