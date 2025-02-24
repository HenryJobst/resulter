package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Collection;

@ValueObject
public record PersonResults(Collection<PersonResult> value) {
    public static PersonResults of(Collection<PersonResult> personResults) {
        return new PersonResults(personResults);
    }
}
