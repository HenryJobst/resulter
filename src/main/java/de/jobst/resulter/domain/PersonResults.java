package de.jobst.resulter.domain;

import java.util.Collection;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record PersonResults(Collection<PersonResult> value) {
    public static PersonResults of(Collection<PersonResult> personResults) {
        return new PersonResults(personResults);
    }
}
