package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record CupId(long value) implements Comparable<CupId> {

    @Override
    public int compareTo(CupId o) {
        return Long.compare(value, o.value);
    }

    public static CupId of(long value) {
        if (value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0.");
        }
        return new CupId(value);
    }

    public static CupId empty() {
        return new CupId(0L);
    }

    public boolean isPersistent() {
        return value != empty().value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
