package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.OrganisationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@Table(name = "organisation")
public class OrganisationDbo {

    @Id
    @With
    private Long id;

    private String name;

    private String shortName;

    private OrganisationType type;

    @MappedCollection(idColumn = "parent_organisation_id")
    private Set<OrganisationOrganisationDbo> childOrganisations = new HashSet<>();

    @Column(value = "country_id")
    private AggregateReference<CountryDbo, Long> country;

    public OrganisationDbo(String name) {
        this.id = null;
        this.name = name;
    }

    public static OrganisationDbo from(Organisation organisation, @NonNull DboResolvers dboResolvers) {
        if (null == organisation) {
            return null;
        }
        OrganisationDbo organisationDbo;
        if (organisation.getId().isPersistent()) {
            organisationDbo = dboResolvers.getOrganisationDboResolver().findDboById(organisation.getId());
            organisationDbo.setName(organisation.getName().value());
        } else {
            organisationDbo = new OrganisationDbo(organisation.getName().value());
        }

        organisationDbo.setShortName(organisation.getShortName().value());

        organisationDbo.setType(organisation.getType());

        if (organisation.getCountryId() != null) {
            organisationDbo.setCountry(AggregateReference.to(organisation.getCountryId().value()));
        } else {
            organisationDbo.setCountry(null);
        }

        organisationDbo.setChildOrganisations(organisation.getChildOrganisationIds()
            .stream()
            .map(it -> new OrganisationOrganisationDbo(it.value()))
            .collect(Collectors.toSet()));

        return organisationDbo;
    }

    public Organisation asOrganisation() {
        return Organisation.of(id,
            name,
            shortName,
            type.value(),
            country != null ? CountryId.of(country.getId()) : null,
            childOrganisations == null ?
            new ArrayList<>() :
            childOrganisations.stream().map(x -> OrganisationId.of(x.id.getId())).toList());
    }

}
