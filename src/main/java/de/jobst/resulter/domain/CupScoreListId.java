package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

import java.util.Objects;

@ValueObject
public record CupScoreListId(Long value) implements Comparable<CupScoreListId> {

    public static CupScoreListId of(Long value) {
        if (value != null && value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0 or null.");
        }
        return new CupScoreListId(value);
    }

    public static CupScoreListId empty() {
        return new CupScoreListId(0L);
    }

    public boolean isPersistent() {
        return !Objects.equals(value, empty().value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }

    public int compareTo(@NonNull CupScoreListId o) {
        return Long.compare(value, o.value);
    }
}
