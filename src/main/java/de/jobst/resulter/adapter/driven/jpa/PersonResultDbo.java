package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.PersonResult;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "PERSON_RESULT")
@NoArgsConstructor
public class PersonResultDbo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_generator")
    @SequenceGenerator(name = "entity_generator", sequenceName = "SEQ_PERSON_RESULT_ID", allocationSize = 10)
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CLASS_RESULT_ID")
    private ClassResultDbo classResultDbo;

    @ManyToOne
    @JoinColumn(name = "PERSON_ID")
    private PersonDbo person;

    @ManyToOne
    @JoinColumn(name = "ORGANISATION_ID")
    private OrganisationDbo organisation;

    @OneToMany(mappedBy = "personResultDbo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PersonRaceResultDbo> personRaceResults = new HashSet<>();

    public static PersonResultDbo from(PersonResult personResult, ClassResultDbo classResultDbo) {
        PersonResultDbo personResultDbo = new PersonResultDbo();
        personResultDbo.setClassResultDbo(classResultDbo);
        personResultDbo.setPerson(PersonDbo.from(personResult.person()));
        if (ObjectUtils.isNotEmpty(personResult.organisation())) {
            personResultDbo.setOrganisation(OrganisationDbo.from(personResult.organisation()));
        }
        personResultDbo.setPersonRaceResults(
                personResult.personRaceResults().value().stream().map(
                        it -> PersonRaceResultDbo.from(it, personResultDbo)).collect(Collectors.toSet()));
        return personResultDbo;
    }

    public PersonResult asPersonResult() {
        return PersonResult.of(person.asPerson(),
                ObjectUtils.isNotEmpty(organisation) ?
                        organisation.asOrganisation() : null,
                personRaceResults.stream().map(PersonRaceResultDbo::asPersonRaceResult).toList());
    }
}