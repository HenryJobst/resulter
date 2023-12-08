package de.jobst.resulter.domain;

import java.util.Collection;

public record PersonResults(Collection<PersonResult> personResults) {
    public static PersonResults of(Collection<PersonResult> personResults) {
        return new PersonResults(personResults);
    }
}
