package de.jobst.resulter.domain;

import de.jobst.resulter.domain.aggregations.PersonRaceResults;
import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;

@ValueObject
public record PersonResult(@NonNull ClassResultShortName classResultShortName, @NonNull PersonId personId,
                           @Nullable OrganisationId organisationId, @NonNull PersonRaceResults personRaceResults)
    implements Comparable<PersonResult> {

    public static PersonResult of(@NonNull ClassResultShortName classResultShortName,
                                  @NonNull PersonId personId,
                                  @Nullable OrganisationId organisationId,
                                  @Nullable Collection<PersonRaceResult> personRaceResults) {
        return new PersonResult(classResultShortName,
            personId,
            organisationId,
            PersonRaceResults.of(personRaceResults));
    }

    @Override
    public int compareTo(@NonNull PersonResult o) {
        int val = personId.compareTo(o.personId);
        if (val == 0) {
            val = classResultShortName.compareTo(o.classResultShortName);
        }
        return val;
    }
}
