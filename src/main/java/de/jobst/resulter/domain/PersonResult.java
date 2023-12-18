package de.jobst.resulter.domain;

import java.util.Collection;
import java.util.Optional;

public record PersonResult(PersonResultId id,
                           Person person,
                           Organisation organisation,
                           Optional<PersonRaceResults> personRaceResults) {
    public static PersonResult of(
            Person person,
            Organisation organisation,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            Optional<Collection<PersonRaceResult>> personRaceResults) {
        return new PersonResult(
                PersonResultId.of(0L),
                person, organisation,
                personRaceResults.map(PersonRaceResults::of));
    }
}
