package de.jobst.resulter.domain;

import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

@ValueObject
public record SplitTimeListId(Long value) {

    public static SplitTimeListId of(Long value) {
        if (value != null && value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0 or null.");
        }
        return new SplitTimeListId(value);
    }

    public static SplitTimeListId empty() {
        return new SplitTimeListId(0L);
    }

    public boolean isPersistent() {
        return !Objects.equals(value, empty().value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }

    public int compareTo(@NonNull SplitTimeListId o) {
        return Long.compare(value, o.value);
    }
}
