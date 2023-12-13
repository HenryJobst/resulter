package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

import java.util.Collection;

public record ClassResult(ClassResultId id,
                          ClassResultName classResultName, ClassResultShortName classResultShortName, Gender gender,
                          PersonResults personResults) {
    public static ClassResult of(@NonNull String name,
                                 @NonNull String shortName,
                                 @NonNull Gender gender,
                                 Collection<PersonResult> personResults) {
        return new ClassResult(
                ClassResultId.of(0L), ClassResultName.of(name), ClassResultShortName.of(shortName),
                gender,
                PersonResults.of(personResults));
    }
}
