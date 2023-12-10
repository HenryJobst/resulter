package de.jobst.resulter.domain;

import java.util.Collection;

public record PersonResults(Collection<PersonResult> value) {
    public static PersonResults of(Collection<PersonResult> personResults) {
        return new PersonResults(personResults);
    }
}
