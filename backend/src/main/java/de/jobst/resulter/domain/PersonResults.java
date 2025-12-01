package de.jobst.resulter.domain;

import java.util.Collection;
import java.util.List;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

@ValueObject
public record PersonResults(Collection<PersonResult> value) {
    public static PersonResults of(@Nullable Collection<PersonResult> personResults) {
        return new PersonResults(personResults != null ? personResults : List.of());
    }
}
