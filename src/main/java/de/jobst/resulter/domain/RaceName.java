package de.jobst.resulter.domain;

import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

@ValueObject
public record RaceName(String value) implements Comparable<RaceName> {

    public static RaceName of(String name) {
        return new RaceName(name);
    }

    public static class NullSafeStringComparator {

        public static int compare(String s1, String s2) {
            if (s1 == null && s2 == null) {
                return 0;
            }
            if (s1 == null) {
                return 1;
            }
            if (s2 == null) {
                return -1;
            }
            return s1.compareTo(s2);
        }
    }

    @Override
    public int compareTo(@NonNull RaceName o) {
        return Objects.compare(this.value, o.value, NullSafeStringComparator::compare);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
