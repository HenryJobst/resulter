package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ShallowLoadProxy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;

@Getter
public class ClassResult {
    @NonNull
    @Setter
    private ClassResultId id;
    @NonNull
    private final EventId eventId;
    @NonNull
    private final ClassResultName classResultName;
    @NonNull
    private final ClassResultShortName classResultShortName;
    @NonNull
    private final Gender gender;
    @NonNull
    private final ShallowLoadProxy<PersonResults> personResults;

    public ClassResult(@NonNull ClassResultId id,
                       @NonNull EventId eventId,
                       @NonNull ClassResultName classResultName,
                       @NonNull ClassResultShortName classResultShortName,
                       @NonNull Gender gender,
                       @NonNull ShallowLoadProxy<PersonResults> personResults) {
        this.id = id;
        this.eventId = eventId;
        this.classResultName = classResultName;
        this.classResultShortName = classResultShortName;
        this.gender = gender;
        this.personResults = personResults;
    }

    public static ClassResult of(@NonNull String name,
                                 @NonNull String shortName,
                                 @NonNull Gender gender,
                                 @Nullable Collection<PersonResult> personResults) {
        return ClassResult.of(ClassResultId.empty().value(),
                EventId.empty().value(), name, shortName, gender, personResults);
    }

    public static ClassResult of(long eventId,
                                 @NonNull String name,
                                 @NonNull String shortName,
                                 @NonNull Gender gender,
                                 @Nullable Collection<PersonResult> personResults) {
        return ClassResult.of(ClassResultId.empty().value(),
                eventId, name, shortName, gender, personResults);
    }

    public static ClassResult of(long id,
                                 long eventId,
                                 @NonNull String name,
                                 @NonNull String shortName,
                                 @NonNull Gender gender,
                                 @Nullable Collection<PersonResult> personResults) {
        return new ClassResult(
                ClassResultId.of(id),
                EventId.of(eventId),
                ClassResultName.of(name), ClassResultShortName.of(shortName),
                gender,
                (personResults != null) ?
                        ShallowLoadProxy.of(PersonResults.of(personResults)) :
                        ShallowLoadProxy.empty());
    }
}
