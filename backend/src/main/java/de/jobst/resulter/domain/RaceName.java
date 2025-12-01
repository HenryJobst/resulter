package de.jobst.resulter.domain;

import java.util.Comparator;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

@ValueObject
public record RaceName(@Nullable String value) implements Comparable<RaceName> {

    public static RaceName of(@Nullable String name) {
        return new RaceName(name);
    }

    private static final Comparator<RaceName> COMPARATOR =
        Comparator.comparing(RaceName::value, Comparator.nullsLast(Comparator.naturalOrder()));

    @Override
    public int compareTo(RaceName o) {
        return COMPARATOR.compare(this, o);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
