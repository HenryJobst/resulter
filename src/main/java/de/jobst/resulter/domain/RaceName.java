package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

import java.util.Objects;

public record RaceName(String value) implements Comparable<RaceName> {

    public static RaceName of(String name) {
        return new RaceName(name);
    }

    @Override
    public int compareTo(@NonNull RaceName o) {
        return Objects.compare(value, o.value, String::compareTo);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
