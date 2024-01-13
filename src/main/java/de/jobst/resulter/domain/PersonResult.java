package de.jobst.resulter.domain;

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
    @Nullable
    private final PersonId personId;
    @Nullable
    private final OrganisationId organisationId;
    @NonNull
    private final PersonRaceResults personRaceResults;

    public PersonResult(@NonNull PersonResultId id,
                        @NonNull ClassResultId classResultId,
                        @Nullable PersonId personId,
                        @Nullable OrganisationId organisationId,
                        @NonNull PersonRaceResults personRaceResults) {
        this.id = id;
        this.classResultId = classResultId;
        this.personId = personId;
        this.organisationId = organisationId;
        this.personRaceResults = personRaceResults;
    }

    public static PersonResult of(@Nullable PersonId personId,
                                  @Nullable OrganisationId organisationId,
                                  @Nullable Collection<PersonRaceResult> personRaceResults) {
        return PersonResult.of(PersonResultId.empty().value(),
            ClassResultId.empty().value(),
            personId,
            organisationId,
            personRaceResults);
    }

    public static PersonResult of(@NonNull Long classResultId,
                                  @Nullable PersonId personId,
                                  @Nullable OrganisationId organisationId,
                                  @Nullable Collection<PersonRaceResult> personRaceResults) {
        return PersonResult.of(PersonResultId.empty().value(),
            classResultId,
            personId,
            organisationId,
            personRaceResults);
    }

    public static PersonResult of(long id,
                                  long classResultId,
                                  @Nullable PersonId personId,
                                  @Nullable OrganisationId organisationId,
                                  @Nullable Collection<PersonRaceResult> personRaceResults) {
        return new PersonResult(PersonResultId.of(id),
            ClassResultId.of(classResultId),
            personId,
            organisationId,
            PersonRaceResults.of(personRaceResults));
    }
}
