package de.jobst.resulter.domain;

import java.util.Collection;

public record PersonResult(Person person, Organisation organisation, PersonRaceResults personRaceResults) {
    public static PersonResult of(
            Person person, Organisation organisation, Collection<PersonRaceResult> personRaceResults) {
        return new PersonResult(person, organisation, PersonRaceResults.of(personRaceResults));
    }
}
