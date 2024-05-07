package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.time.ZoneId;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@NoArgsConstructor
@Table(name = "person_result")
public class PersonResultDbo {

    @NonNull
    @Column("class_result_short_name")
    private String classResultShortName;

    @NonNull
    @Column("person_id")
    private AggregateReference<PersonDbo, Long> person;

    @Column("organisation_id")
    private AggregateReference<OrganisationDbo, Long> organisation;

    @MappedCollection(idColumn = "result_list_id")
    private Set<PersonRaceResultDbo> personRaceResults = new HashSet<>();

    public static PersonResultDbo from(@NonNull PersonResult personResult) {
        PersonResultDbo personResultDbo = new PersonResultDbo();
        personResultDbo.setClassResultShortName(Objects.requireNonNull(Objects.requireNonNull(personResult.classResultShortName())
            .value()));
        personResultDbo.setPerson(AggregateReference.to(Objects.requireNonNull(Objects.requireNonNull(personResult.personId())
            .value())));

        personResultDbo.setOrganisation(personResult.organisationId() != null ?
                                        AggregateReference.to(personResult.organisationId().value()) :
                                        null);

        personResultDbo.setPersonRaceResults(personResult.personRaceResults()
            .value()
            .stream()
            .map(PersonRaceResultDbo::from)
            .collect(Collectors.toSet()));

        return personResultDbo;
    }

    static public Collection<PersonResult> asPersonResults(@NonNull Collection<PersonResultDbo> personResultDbos) {
        return personResultDbos.stream()
            .map(it -> PersonResult.of(ClassResultShortName.of(it.classResultShortName),
                PersonId.of(it.person.getId()),
                it.organisation != null ? OrganisationId.of(it.organisation.getId()) : null,
                it.getPersonRaceResults()
                    .stream()
                    .map(x -> PersonRaceResult.of(it.classResultShortName,
                        it.person.getId(),
                        x.getStartTime() != null ?
                        x.getStartTime().atZoneSameInstant(ZoneId.of(Objects.requireNonNull(x.getStartTimeZone()))) :
                        null,
                        x.getFinishTime() != null ?
                        x.getFinishTime().atZoneSameInstant(ZoneId.of(x.getFinishTimeZone())) :
                        null,
                        x.getPunchTime(),
                        x.getPosition(),
                        x.getRaceNumber(),
                        x.getState()))
                    .toList()))
            .toList();
    }
}
