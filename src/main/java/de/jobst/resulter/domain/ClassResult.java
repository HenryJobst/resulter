package de.jobst.resulter.domain;

import java.util.Collection;

public record ClassResult(ClassResultName classResultName, ClassResultShortName classResultShortName, Gender gender,
                          PersonResults personResults) {
    public static ClassResult of(String name, String shortName, String gender, Collection<PersonResult> personResults) {
        return new ClassResult(ClassResultName.of(name), ClassResultShortName.of(shortName),
                Gender.of(gender),
                PersonResults.of(personResults));
    }
}
