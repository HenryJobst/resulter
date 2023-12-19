package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public record PersonResult(PersonResultId id,
                           ClassResultId classResultId,
                           Optional<Person> person,
                           Optional<Organisation> organisation,
                           Optional<PersonRaceResults> personRaceResults) {
    public static PersonResult of(
            @NonNull Optional<Person> person,
            @NonNull Optional<Organisation> organisation,
            @NonNull Optional<Collection<PersonRaceResult>> personRaceResults) {
        return PersonResult.of(
                PersonResultId.empty().value(),
                ClassResultId.empty().value(),
                person, organisation,
                personRaceResults);
    }

    public static PersonResult of(
            @NonNull Long classResultId,
            @NonNull Optional<Person> person,
            @NonNull Optional<Organisation> organisation,
            @NonNull Optional<Collection<PersonRaceResult>> personRaceResults) {
        return new PersonResult(
                PersonResultId.empty(),
                ClassResultId.of(classResultId),
                person, organisation,
                personRaceResults.map(PersonRaceResults::of));
    }

    public static PersonResult of(
            @NonNull Long id,
            @NonNull Long classResultId,
            @NonNull Optional<Person> person,
            @NonNull Optional<Organisation> organisation,
            @NonNull Optional<Collection<PersonRaceResult>> personRaceResults) {
        return new PersonResult(
                PersonResultId.of(id),
                ClassResultId.of(classResultId),
                person,
                organisation,
                personRaceResults.map(PersonRaceResults::of));
    }
}
