package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.*;
import jakarta.persistence.*;
import org.hibernate.Hibernate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed", "unused"})
@Entity
@Table(name = "PERSON_RESULT")
public class PersonResultDbo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_generator_person_result")
    @SequenceGenerator(name = "entity_generator_person_result", sequenceName = "SEQ_PERSON_RESULT_ID",
                       allocationSize = 10)
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

    public static PersonResultDbo from(@NonNull PersonResult personResult,
                                       @NonNull ClassResultDbo classResultDbo,
                                       @Nullable DboResolver<PersonResultId, PersonResultDbo> dboResolver,
                                       @NonNull DboResolvers dboResolvers) {
        PersonResultDbo personResultDbo = null;
        PersonResultDbo persistedPersonResultDbo;
        if (personResult.getId().isPersistent()) {
            if (dboResolver != null) {
                personResultDbo = dboResolver.findDboById(personResult.getId());
            }
            if (personResultDbo == null) {
                personResultDbo = dboResolvers.getPersonResultDboResolver().findDboById(personResult.getId());
            }
            persistedPersonResultDbo = personResultDbo;
        } else {
            personResultDbo = new PersonResultDbo();
            persistedPersonResultDbo = null;
        }
        personResultDbo.setClassResultDbo(classResultDbo);

        personResultDbo.setPerson(personResult.getPersonId() != null ?
                                  dboResolvers.getPersonDboResolver().findDboById(personResult.getPersonId()) :
                                  null);

        personResultDbo.setOrganisation(personResult.getOrganisationId() != null ?
                                        dboResolvers.getOrganisationDboResolver()
                                            .findDboById(personResult.getOrganisationId()) :
                                        null);

        if (personResult.getPersonRaceResults().isLoaded()) {
            PersonResultDbo finalPersonResultDbo = personResultDbo;
            personResultDbo.setPersonRaceResults(personResult.getPersonRaceResults().get().value().stream().map(it -> {
                PersonRaceResultDbo persistedPersonRaceResultDbo = persistedPersonResultDbo != null &&
                                                                   Hibernate.isInitialized(persistedPersonResultDbo.getPersonRaceResults()) ?
                                                                   (persistedPersonResultDbo.getPersonRaceResults()
                                                                        .stream()
                                                                        .filter(x -> x.getId() == it.getId().value())
                                                                        .findFirst()
                                                                        .orElse(null)) :
                                                                   null;
                return PersonRaceResultDbo.from(it,
                    finalPersonResultDbo,
                    (id) -> persistedPersonRaceResultDbo,
                    dboResolvers);
            }).collect(Collectors.toSet()));
        } else if (persistedPersonResultDbo != null) {
            personResultDbo.setPersonRaceResults(persistedPersonResultDbo.getPersonRaceResults());
        } else if (personResult.getId().isPersistent()) {
            throw new IllegalArgumentException();
        }
        return personResultDbo;
    }

    static public Collection<PersonResult> asPersonResults(@NonNull EventConfig eventConfig,
                                                           @NonNull Collection<PersonResultDbo> personResultDbos) {
        Map<PersonResultId, List<PersonRaceResult>> personRaceResultsByPersonResultId;
        if (!eventConfig.shallowLoads().contains(EventConfig.ShallowEventLoads.PERSON_RACE_RESULTS)) {
            personRaceResultsByPersonResultId = PersonRaceResultDbo.asPersonRaceResults(eventConfig,
                    personResultDbos.stream().flatMap(x -> x.personRaceResults.stream()).toList())
                .stream()
                .collect(Collectors.groupingBy(PersonRaceResult::getPersonResultId));
        } else {
            personRaceResultsByPersonResultId = null;
        }
        return personResultDbos.stream()
            .map(it -> PersonResult.of(it.id,
                it.getClassResultDbo() != null ? it.getClassResultDbo().getId() : ClassResultId.empty().value(),
                eventConfig.shallowLoads().contains(EventConfig.ShallowEventLoads.PERSONS) ?
                null :
                it.person != null ? PersonId.of(it.person.getId()) : null,
                it.organisation != null ? OrganisationId.of(it.organisation.getId()) : null,
                personRaceResultsByPersonResultId == null ?
                null :
                personRaceResultsByPersonResultId.getOrDefault(PersonResultId.of(it.id), new ArrayList<>())))
            .toList();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ClassResultDbo getClassResultDbo() {
        return classResultDbo;
    }

    public void setClassResultDbo(ClassResultDbo classResultDbo) {
        this.classResultDbo = classResultDbo;
    }

    public PersonDbo getPerson() {
        return person;
    }

    public void setPerson(PersonDbo person) {
        this.person = person;
    }

    public OrganisationDbo getOrganisation() {
        return organisation;
    }

    public void setOrganisation(OrganisationDbo organisation) {
        this.organisation = organisation;
    }

    public Set<PersonRaceResultDbo> getPersonRaceResults() {
        return personRaceResults;
    }

    public void setPersonRaceResults(Set<PersonRaceResultDbo> personRaceResults) {
        this.personRaceResults = personRaceResults;
    }
}
