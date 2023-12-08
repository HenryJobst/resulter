package de.jobst.resulter.domain;

import java.util.Collection;

public record PersonRaceResults(Collection<PersonRaceResult> personRaceResults) {
    public static PersonRaceResults of(Collection<PersonRaceResult> personRaceResults) {
        return new PersonRaceResults(personRaceResults);
    }
}
