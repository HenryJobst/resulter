package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

@ValueObject
public record SplitTimeId(long value) implements Comparable<SplitTimeId> {

    public static SplitTimeId of(long value) {
        if (value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0.");
        }
        return new SplitTimeId(value);
    }

    public static SplitTimeId empty() {
        return new SplitTimeId(0L);
    }

    public boolean isPersistent() {
        return value != empty().value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }

    @Override
    public int compareTo(@NonNull SplitTimeId o) {
        return Long.compare(value, o.value);
    }
}
