package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ShallowLoadProxy;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;

public record ClassResult(ClassResultId id,
                          EventId eventId,
                          ClassResultName classResultName,
                          ClassResultShortName classResultShortName,
                          Gender gender,
                          ShallowLoadProxy<PersonResults> personResults) {
    public static ClassResult of(@NonNull String name,
                                 @NonNull String shortName,
                                 @NonNull Gender gender,
                                 @Nullable Collection<PersonResult> personResults) {
        return ClassResult.of(ClassResultId.empty().value(),
                EventId.empty().value(), name, shortName, gender, personResults);
    }

    public static ClassResult of(@NonNull Long eventId,
                                 @NonNull String name,
                                 @NonNull String shortName,
                                 @NonNull Gender gender,
                                 @Nullable Collection<PersonResult> personResults) {
        return ClassResult.of(ClassResultId.empty().value(),
                eventId, name, shortName, gender, personResults);
    }

    public static ClassResult of(@NonNull Long classResultId,
                                 @NonNull Long eventId,
                                 @NonNull String name,
                                 @NonNull String shortName,
                                 @NonNull Gender gender,
                                 @Nullable Collection<PersonResult> personResults) {
        return new ClassResult(
                ClassResultId.of(classResultId),
                EventId.of(eventId),
                ClassResultName.of(name), ClassResultShortName.of(shortName),
                gender,
                (personResults != null) ?
                        ShallowLoadProxy.of(PersonResults.of(personResults)) :
                        ShallowLoadProxy.empty());
    }
}
