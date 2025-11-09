package de.jobst.resulter.domain;

import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

@ValueObject
public record MediaFileId(Long value) implements Comparable<MediaFileId> {

    public static MediaFileId of(Long value) {
        if (value != null && value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0 or null.");
        }
        return new MediaFileId(value);
    }

    public static MediaFileId empty() {
        return new MediaFileId(0L);
    }

    @Override
    public int compareTo(@NonNull MediaFileId o) {
        return value.compareTo(o.value);
    }

    public boolean isPersistent() {
        return !Objects.equals(value, empty().value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
