package de.jobst.resulter.domain;

import java.util.Collection;

public record PersonResult(PersonResultId id,
                           Person person,
                           Organisation organisation,
                           PersonRaceResults personRaceResults) {
    public static PersonResult of(
            Person person, Organisation organisation, Collection<PersonRaceResult> personRaceResults) {
        return new PersonResult(
                PersonResultId.of(0L),
                person, organisation, PersonRaceResults.of(personRaceResults));
    }
}
