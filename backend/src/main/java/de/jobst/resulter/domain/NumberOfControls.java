package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@ValueObject
public record NumberOfControls(@Nullable Integer value) implements Comparable<NumberOfControls> {

    public static NumberOfControls of(@Nullable Integer numberOfControls) {
        return new NumberOfControls(numberOfControls);
    }

    @Override
    public int compareTo(NumberOfControls o) {
        return Objects.compare(this, o, NumberOfControls::compareTo);
    }
}
