package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.PersonRaceResult;
import de.jobst.resulter.domain.PersonResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@NoArgsConstructor
@Table(name = "PERSON_RESULT")
public class PersonResultDbo {

    private AggregateReference<PersonDbo, Long> person;

    private AggregateReference<OrganisationDbo, Long> organisation;

    private Set<PersonRaceResultDbo> personRaceResults = new HashSet<>();

    public static PersonResultDbo from(@NonNull PersonResult personResult, @NonNull DboResolvers dboResolvers) {
        PersonResultDbo personResultDbo = new PersonResultDbo();

        personResultDbo.setPerson(
            personResult.personId() != null ? AggregateReference.to(personResult.personId().value()) : null);

        personResultDbo.setOrganisation(personResult.organisationId() != null ?
                                        AggregateReference.to(personResult.organisationId().value()) :
                                        null);

        personResultDbo.setPersonRaceResults(personResult.personRaceResults()
            .value()
            .stream()
            .map(x -> PersonRaceResultDbo.from(x, personResultDbo))
            .collect(Collectors.toSet()));

        return personResultDbo;
    }

    static public Collection<PersonResult> asPersonResults(@NonNull Collection<PersonResultDbo> personResultDbos) {
        return personResultDbos.stream()
            .map(it -> PersonResult.of(it.person != null ? PersonId.of(it.person.getId()) : null,
                it.organisation != null ? OrganisationId.of(it.organisation.getId()) : null,
                it.getPersonRaceResults()
                    .stream()
                    .map(x -> PersonRaceResult.of(x.getRaceNumber(),
                        x.getStartTime(),
                        x.getFinishTime(),
                        x.getPunchTime(),
                        x.getPosition(),
                        x.getState()))
                    .toList()))
            .toList();
    }
}
