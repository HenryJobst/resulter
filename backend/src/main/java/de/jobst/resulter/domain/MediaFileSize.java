package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record MediaFileSize(Long value) implements Comparable<MediaFileSize> {

    public static MediaFileSize of(Long size) {
        ValueObjectChecks.requireGreaterZero(size);
        return new MediaFileSize(size);
    }

    @Override
    public int compareTo(MediaFileSize o) {
        return value.compareTo(o.value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
