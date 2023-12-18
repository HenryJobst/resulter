package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Optional;

public record PersonResult(PersonResultId id,
                           ClassResultId classResultId,
                           Person person,
                           Organisation organisation,
                           Optional<PersonRaceResults> personRaceResults) {
    public static PersonResult of(
            Person person,
            Organisation organisation,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            Optional<Collection<PersonRaceResult>> personRaceResults) {
        return PersonResult.of(
                PersonResultId.empty().value(),
                ClassResultId.empty().value(),
                person, organisation,
                personRaceResults);
    }

    public static PersonResult of(
            Long classResultId,
            Person person,
            Organisation organisation,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            Optional<Collection<PersonRaceResult>> personRaceResults) {
        return new PersonResult(
                PersonResultId.empty(),
                ClassResultId.of(classResultId),
                person, organisation,
                personRaceResults.map(PersonRaceResults::of));
    }

    public static PersonResult of(
            @NonNull Long id,
            @NonNull Long classResultId,
            @NonNull Person person,
            @Nullable Organisation organisation,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            Optional<Collection<PersonRaceResult>> personRaceResults) {
        return new PersonResult(
                PersonResultId.of(id),
                ClassResultId.of(classResultId),
                person,
                organisation,
                personRaceResults.map(PersonRaceResults::of));
    }
}
