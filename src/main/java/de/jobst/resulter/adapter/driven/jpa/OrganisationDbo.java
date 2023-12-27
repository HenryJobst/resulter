package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

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

    @OneToMany(mappedBy = "organisation", fetch = FetchType.LAZY)
    private Set<PersonResultDbo> personResults = new HashSet<>();

    @ManyToMany(mappedBy = "organisations", fetch = FetchType.LAZY)
    private Set<EventDbo> events = new HashSet<>();

    public static OrganisationDbo from(Organisation organisation) {
        if (null == organisation) {
            return null;
        }
        OrganisationDbo organisationDbo = new OrganisationDbo();
        if (organisation.getId().value() != OrganisationId.empty().value()) {
            organisationDbo.setId(organisation.getId().value());
        }
        organisationDbo.setName(organisation.getName().value());
        organisationDbo.setShortName(organisation.getShortName().value());
        return organisationDbo;
    }

    public Organisation asOrganisation() {
        return Organisation.of(id, name, shortName);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }
}