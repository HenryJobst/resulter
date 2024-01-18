package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

import java.util.Objects;

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
