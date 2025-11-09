package de.jobst.resulter.domain;

import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

@ValueObject
public record ResultListId(Long value) {

    public static ResultListId of(Long value) {
        if (value != null && value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0 or null.");
        }
        return new ResultListId(value);
    }

    public static ResultListId empty() {
        return new ResultListId(0L);
    }

    public boolean isPersistent() {
        return !Objects.equals(value, empty().value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }

    public int compareTo(@NonNull ResultListId o) {
        return Long.compare(value, o.value);
    }
}
