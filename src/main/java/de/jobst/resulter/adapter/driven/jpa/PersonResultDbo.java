package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.PersonResult;
import jakarta.persistence.*;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed", "unused"})
@Entity
@Table(name = "PERSON_RESULT")
public class PersonResultDbo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_generator")
    @SequenceGenerator(name = "entity_generator", sequenceName = "SEQ_PERSON_RESULT_ID", allocationSize = 10)
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLASS_RESULT_ID")
    private ClassResultDbo classResultDbo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSON_ID")
    private PersonDbo person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANISATION_ID")
    private OrganisationDbo organisation;

    @OneToMany(mappedBy = "personResultDbo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PersonRaceResultDbo> personRaceResults = new HashSet<>();

    public static PersonResultDbo from(PersonResult personResult, ClassResultDbo classResultDbo) {
        PersonResultDbo personResultDbo = new PersonResultDbo();
        if (personResult.id() != null) {
            personResultDbo.setId(personResult.id().value());
        }
        personResultDbo.setClassResultDbo(classResultDbo);
        personResultDbo.setPerson(PersonDbo.from(personResult.person()));
        if (ObjectUtils.isNotEmpty(personResult.organisation())) {
            personResultDbo.setOrganisation(OrganisationDbo.from(personResult.organisation()));
        }
        personResultDbo.setPersonRaceResults(personResult.personRaceResults()
                .value()
                .stream()
                .map(it -> PersonRaceResultDbo.from(it, personResultDbo))
                .collect(Collectors.toSet()));
        return personResultDbo;
    }

    public PersonResult asPersonResult() {
        return PersonResult.of(person.asPerson(),
                ObjectUtils.isNotEmpty(organisation) ?
                        organisation.asOrganisation() : null,
                personRaceResults.stream().map(PersonRaceResultDbo::asPersonRaceResult).toList()
        );
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setClassResultDbo(ClassResultDbo classResultDbo) {
        this.classResultDbo = classResultDbo;
    }

    public ClassResultDbo getClassResultDbo() {
        return classResultDbo;
    }

    public void setPerson(PersonDbo person) {
        this.person = person;
    }

    public PersonDbo getPerson() {
        return person;
    }

    public void setOrganisation(OrganisationDbo organisation) {
        this.organisation = organisation;
    }

    public OrganisationDbo getOrganisation() {
        return organisation;
    }

    public void setPersonRaceResults(Set<PersonRaceResultDbo> personRaceResults) {
        this.personRaceResults = personRaceResults;
    }

    public Collection<PersonRaceResultDbo> getPersonRaceResults() {
        return personRaceResults;
    }
}