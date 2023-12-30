package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.OrganisationType;
import jakarta.persistence.*;
import org.springframework.lang.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed", "unused"})
@Entity
@Table(name = "ORGANISATION")
public class OrganisationDbo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_generator_organisation")
    @SequenceGenerator(name = "entity_generator_organisation",
            sequenceName = "SEQ_ORGANISATION_ID",
            allocationSize = 10)
    @Column(name = "ID", nullable = false, unique = true)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "SHORT_NAME", nullable = false)
    private String shortName;

    @Column(name = "TYPE", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private OrganisationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUNTRY_ID")
    private CountryDbo country;
    @OneToMany(mappedBy = "organisation", fetch = FetchType.LAZY)
    private Set<PersonResultDbo> personResults = new HashSet<>();
    @ManyToMany(mappedBy = "organisations", fetch = FetchType.LAZY)
    private Set<EventDbo> events = new HashSet<>();
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "ORGANISATION_ORGANISATION",
            joinColumns = @JoinColumn(name = "ORGANISATION_ID"),
            inverseJoinColumns = @JoinColumn(name = "PARENT_ORGANISATION_ID"))
    private Set<OrganisationDbo> parentOrganisations = new HashSet<>();
    @ManyToMany(mappedBy = "parentOrganisations", fetch = FetchType.LAZY)
    private Set<OrganisationDbo> childOrganisations = new HashSet<>();

    public static OrganisationDbo from(Organisation organisation, @Nullable OrganisationDbo persistedOrganisationDbo) {
        if (null == organisation) {
            return null;
        }
        OrganisationDbo organisationDbo = new OrganisationDbo();
        if (organisation.getId().value() != OrganisationId.empty().value()) {
            organisationDbo.setId(organisation.getId().value());
        }
        organisationDbo.setName(organisation.getName().value());
        organisationDbo.setShortName(organisation.getShortName().value());

        organisationDbo.setType(organisation.getType());
        organisationDbo.setCountry(CountryDbo.from(organisation.getCountry().get()));

        if (organisation.getParentOrganisations().isLoaded()) {
            organisationDbo.setParentOrganisations(
                    Objects.requireNonNull(organisation.getParentOrganisations())
                            .get().value()
                            .stream()
                            .map(it -> {
                                OrganisationDbo persistedParentOrganisationDbo =
                                        persistedOrganisationDbo != null ?
                                                (persistedOrganisationDbo.getParentOrganisations()
                                                        .stream()
                                                        .filter(x -> x.getId() == it.getId().value())
                                                        .findFirst()
                                                        .orElse(null))
                                                : null;
                                return OrganisationDbo.from(it, persistedParentOrganisationDbo);
                            })
                            .collect(Collectors.toSet()));
        } else if (persistedOrganisationDbo != null) {
            organisationDbo.setParentOrganisations(persistedOrganisationDbo.getParentOrganisations());
        } else if (organisation.getId().isPersistent()) {
            throw new IllegalArgumentException();
        }

        return organisationDbo;
    }


    public Organisation asOrganisation() {
        return Organisation.of(id, name, shortName, type.value(),
                country != null ? country.asCountry() : null,
                parentOrganisations.stream().map(it -> Organisation.of(
                        it.id, it.name, it.shortName, it.type.value(),
                        it.country != null ? it.country.asCountry() : null,
                        null
                )).toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public OrganisationType getType() {
        return type;
    }

    public void setType(OrganisationType type) {
        this.type = type;
    }

    public CountryDbo getCountry() {
        return country;
    }

    public void setCountry(CountryDbo country) {
        this.country = country;
    }

    public Set<OrganisationDbo> getParentOrganisations() {
        return parentOrganisations;
    }

    public void setParentOrganisations(Set<OrganisationDbo> parentOrganisations) {
        this.parentOrganisations = parentOrganisations;
    }

    public Set<OrganisationDbo> getChildOrganisations() {
        return childOrganisations;
    }

    public void setChildOrganisations(Set<OrganisationDbo> childOrganisations) {
        this.childOrganisations = childOrganisations;
    }

    public Set<PersonResultDbo> getPersonResults() {
        return personResults;
    }

    public void setPersonResults(Set<PersonResultDbo> personResults) {
        this.personResults = personResults;
    }
}