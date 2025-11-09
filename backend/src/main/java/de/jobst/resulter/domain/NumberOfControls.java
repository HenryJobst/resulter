package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

@ValueObject
public record NumberOfControls(Integer value) implements Comparable<NumberOfControls> {

    public static NumberOfControls of(Integer numberOfControls) {
        return new NumberOfControls(numberOfControls);
    }

    @Override
    public int compareTo(@NonNull NumberOfControls o) {
        return value.compareTo(o.value);
    }
}
