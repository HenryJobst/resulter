package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.*;
import jakarta.persistence.*;
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
    @SequenceGenerator(name = "entity_generator_person_result",
            sequenceName = "SEQ_PERSON_RESULT_ID",
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
                                       @Nullable PersonResultDbo persistedPersonResultDbo) {
        PersonResultDbo personResultDbo = new PersonResultDbo();
        if (personResult.getId().value() != PersonResultId.empty().value()) {
            personResultDbo.setId(personResult.getId().value());
        }
        personResultDbo.setClassResultDbo(classResultDbo);

        if (personResult.getPerson().isLoaded()) {
            personResultDbo.setPerson(PersonDbo.from(personResult.getPerson().get(),
                    persistedPersonResultDbo != null ? persistedPersonResultDbo.person : null));
        } else if (persistedPersonResultDbo != null) {
            personResultDbo.setPerson(persistedPersonResultDbo.getPerson());
        } else if (personResult.getId().isPersistent()) {
            throw new IllegalArgumentException();
        }

        if (personResult.getOrganisation().isLoaded()) {
            OrganisationDbo persistedOrganisationDbo =
                    persistedPersonResultDbo != null ? persistedPersonResultDbo.getOrganisation() : null;
            personResultDbo.setOrganisation(OrganisationDbo.from(personResult.getOrganisation().get(),
                    persistedOrganisationDbo));
        } else if (persistedPersonResultDbo != null) {
            personResultDbo.setOrganisation(persistedPersonResultDbo.getOrganisation());
        } else if (personResult.getId().isPersistent()) {
            throw new IllegalArgumentException();
        }

        if (personResult.getPersonRaceResults().isLoaded()) {
            personResultDbo.setPersonRaceResults(personResult.getPersonRaceResults().get()
                    .value()
                    .stream()
                    .map(it -> {
                        PersonRaceResultDbo persistedPersonRaceResultDbo =
                                persistedPersonResultDbo != null ?
                                        (persistedPersonResultDbo.getPersonRaceResults()
                                                .stream()
                                                .filter(x -> x.getId() == it.getId().value())
                                                .findFirst()
                                                .orElse(null))
                                        : null;
                        return PersonRaceResultDbo.from(it, personResultDbo, persistedPersonRaceResultDbo);
                    })
                    .collect(Collectors.toSet()));
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
        if (!eventConfig.shallowLoads().contains(EventConfig.ShallowLoads.PERSON_RACE_RESULTS)) {
            personRaceResultsByPersonResultId =
                    PersonRaceResultDbo.asPersonRaceResults(eventConfig,
                                    personResultDbos.stream().flatMap(x -> x.personRaceResults.stream()).toList())
                            .stream()
                            .collect(Collectors.groupingBy(PersonRaceResult::getPersonResultId));
        } else {
            personRaceResultsByPersonResultId = null;
        }
        return personResultDbos.stream()
                .map(
                        it -> PersonResult.of(
                                it.id,
                                it.getClassResultDbo() != null ?
                                        it.getClassResultDbo().getId() :
                                        ClassResultId.empty().value(),
                                eventConfig.shallowLoads().contains(EventConfig.ShallowLoads.PERSONS) ?
                                        null : (it.person != null ? it.person.asPerson() : null),
                                eventConfig.shallowLoads().contains(EventConfig.ShallowLoads.ORGANISATIONS) ?
                                        null :
                                        (it.organisation != null ? it.organisation.asOrganisation() : null),
                                personRaceResultsByPersonResultId == null ? null :
                                        personRaceResultsByPersonResultId.getOrDefault(PersonResultId.of(it.id),
                                                new ArrayList<>())))
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