package de.jobst.resulter.adapter.driven.jpa;

import de.jobst.resulter.domain.Organisation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ORGANISATION")
@Getter
@Setter
@NoArgsConstructor
public class OrganisationDbo {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_generator")
    @SequenceGenerator(name = "entity_generator", sequenceName = "SEQ_ORGANISATION_ID", allocationSize = 10)
    @Column(name = "ID", nullable = false, unique = true)
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "SHORT_NAME", nullable = false)
    private String shortName;

    public static OrganisationDbo from(Organisation organisation) {
        OrganisationDbo organisationDbo = new OrganisationDbo();
        if (organisation.getId() != null) {
            organisationDbo.setId(organisation.getId().value());
        }
        organisationDbo.setName(organisation.getName().value());
        organisationDbo.setShortName(organisation.getShortName().value());
        return organisationDbo;
    }

    public Organisation asOrganisation() {
        return Organisation.of(id, name, shortName);
    }
}