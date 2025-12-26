package de.jobst.resulter.domain;

import java.util.Comparator;
import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record ResultListId(Long value) {

    public static ResultListId of(Long value) {
        if (value < 0L) {
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

    private static final Comparator<ResultListId> COMPARATOR =
        Comparator.comparing(ResultListId::value);

    public int compareTo(ResultListId o) {
        return COMPARATOR.compare(this, o);
    }
}
