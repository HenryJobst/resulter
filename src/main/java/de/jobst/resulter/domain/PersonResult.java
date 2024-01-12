package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ShallowLoadProxy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;

@Getter
public class PersonResult {

    @NonNull
    @Setter
    private PersonResultId id;
    @NonNull
    private final ClassResultId classResultId;
    @NonNull
    private final ShallowLoadProxy<Person> person;
    @Nullable
    private final OrganisationId organisationId;
    @NonNull
    private final ShallowLoadProxy<PersonRaceResults> personRaceResults;

    public PersonResult(@NonNull PersonResultId id,
                        @NonNull ClassResultId classResultId,
                        @NonNull ShallowLoadProxy<Person> person,
                        @Nullable OrganisationId organisationId,
                        @NonNull ShallowLoadProxy<PersonRaceResults> personRaceResults) {
        this.id = id;
        this.classResultId = classResultId;
        this.person = person;
        this.organisationId = organisationId;
        this.personRaceResults = personRaceResults;
    }

    public static PersonResult of(@Nullable Person person,
                                  @Nullable OrganisationId organisationId,
                                  @Nullable Collection<PersonRaceResult> personRaceResults) {
        return PersonResult.of(PersonResultId.empty().value(),
            ClassResultId.empty().value(),
            person,
            organisationId,
            personRaceResults);
    }

    public static PersonResult of(@NonNull Long classResultId,
                                  @Nullable Person person,
                                  @Nullable OrganisationId organisationId,
                                  @Nullable Collection<PersonRaceResult> personRaceResults) {
        return PersonResult.of(PersonResultId.empty().value(),
            classResultId,
            person,
            organisationId,
            personRaceResults);
    }

    public static PersonResult of(long id,
                                  long classResultId,
                                  @Nullable Person person,
                                  @Nullable OrganisationId organisationId,
                                  @Nullable Collection<PersonRaceResult> personRaceResults) {
        return new PersonResult(PersonResultId.of(id),
            ClassResultId.of(classResultId),
            ShallowLoadProxy.of(person),
            organisationId,
            (personRaceResults != null) ?
            ShallowLoadProxy.of(PersonRaceResults.of(personRaceResults)) :
            ShallowLoadProxy.empty());
    }
}
