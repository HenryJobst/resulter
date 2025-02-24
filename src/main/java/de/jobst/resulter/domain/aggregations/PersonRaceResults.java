package de.jobst.resulter.domain.aggregations;

import de.jobst.resulter.domain.PersonRaceResult;

import java.util.Collection;

public record PersonRaceResults(Collection<PersonRaceResult> value) {
    public static PersonRaceResults of(Collection<PersonRaceResult> personRaceResults) {
        return new PersonRaceResults(personRaceResults);
    }
}
