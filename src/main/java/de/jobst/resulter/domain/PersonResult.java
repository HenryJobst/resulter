package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;

public record PersonResult(@NonNull ClassResultShortName classResultShortName, @Nullable PersonId personId,
                           @Nullable OrganisationId organisationId, @NonNull PersonRaceResults personRaceResults) {

    public static PersonResult of(@NonNull ClassResultShortName classResultShortName,
                                  @Nullable PersonId personId,
                                  @Nullable OrganisationId organisationId,
                                  @Nullable Collection<PersonRaceResult> personRaceResults) {
        return new PersonResult(classResultShortName,
            personId,
            organisationId,
            PersonRaceResults.of(personRaceResults));
    }
}
