package de.jobst.resulter.domain;

import de.jobst.resulter.domain.aggregations.PersonRaceResults;
import java.util.Collection;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

@ValueObject
public record PersonResult(
        ClassResultShortName classResultShortName,
        PersonId personId,
        @Nullable OrganisationId organisationId,
        PersonRaceResults personRaceResults)
        implements Comparable<PersonResult> {

    public static PersonResult of(
            ClassResultShortName classResultShortName,
            PersonId personId,
            @Nullable OrganisationId organisationId,
            @Nullable Collection<PersonRaceResult> personRaceResults) {
        return new PersonResult(
                classResultShortName, personId, organisationId, PersonRaceResults.of(personRaceResults));
    }

    @Override
    public int compareTo(PersonResult o) {
        int val = personId.compareTo(o.personId);
        if (val == 0) {
            val = classResultShortName.compareTo(o.classResultShortName);
        }
        return val;
    }
}
