package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

import java.util.Objects;

public record ClassResultId(Long value) {

    public static ClassResultId of(Long value) {
        if (value != null && value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0.");
        }
        return new ClassResultId(value);
    }

    public static ClassResultId empty() {
        return new ClassResultId(0L);
    }

    public boolean isPersistent() {
        return !Objects.equals(value, empty().value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }

    public int compareTo(@NonNull ClassResultId o) {
        return Long.compare(value, o.value);
    }
}
