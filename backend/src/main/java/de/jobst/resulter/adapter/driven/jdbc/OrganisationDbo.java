package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ = @PersistenceCreator)
@Table(name = "organisation")
public class OrganisationDbo {

    @Nullable
    @Id
    @With
    @Column("id")
    private Long id;

    @Column("name")
    private String name;

    @Column("short_name")
    private String shortName;

    @Column("type")
    private OrganisationType type;

    @MappedCollection(idColumn = "parent_organisation_id")
    private Set<OrganisationOrganisationDbo> childOrganisations = new HashSet<>();

    @Nullable
    @Column(value = "country_id")
    private AggregateReference<CountryDbo, Long> country;

    public OrganisationDbo(String name) {
        this.id = null;
        this.name = name;
    }

    @SuppressWarnings("ConstantConditions")
    public static OrganisationDbo from(Organisation organisation, DboResolvers dboResolvers) {
        OrganisationDbo organisationDbo;
        if (organisation.getId().isPersistent()) {
            organisationDbo = Objects.requireNonNull(
                    dboResolvers.getOrganisationDboResolver().findDboById(organisation.getId()));
            organisationDbo.setName(organisation.getName().value());
        } else {
            organisationDbo = new OrganisationDbo(organisation.getName().value());
        }

        organisationDbo.setShortName(organisation.getShortName().value());

        organisationDbo.setType(organisation.getType());

        if (organisation.getCountry() != null) {
            organisationDbo.setCountry(
                    AggregateReference.to(organisation.getCountry().value()));
        } else {
            organisationDbo.setCountry(null);
        }

        organisationDbo.setChildOrganisations(organisation.getChildOrganisations().stream()
                .map(it -> new OrganisationOrganisationDbo(it.value()))
                .collect(Collectors.toSet()));

        return organisationDbo;
    }

    public static Organisation asOrganisation(
            OrganisationDbo organisationDbo,
            Function<Long, Organisation> organisationResolver,
            Function<Long, Country> countryResolver) {
        return organisationDbo.asOrganisation(organisationResolver, countryResolver);
    }

    @SuppressWarnings("unused")
    public Organisation asOrganisation(
            Function<Long, Organisation> organisationResolver, Function<Long, Country> countryResolver) {
        return Organisation.of(
                id,
                name,
                shortName,
                type.value(),
                Optional.ofNullable(country).map(x -> CountryId.of(x.getId())).orElse(CountryId.empty()),
                childOrganisations.stream()
                        .map(x -> OrganisationId.of(x.id.getId()))
                        .toList());
    }

    public Organisation asOrganisation(Map<Long, Organisation> organisationMap, Map<Long, Country> countryMap) {
        return Organisation.of(
                id,
                name,
                shortName,
                type.value(),
                Optional.ofNullable(country).map(x -> CountryId.of(x.getId())).orElse(CountryId.empty()),
                childOrganisations.stream()
                        .map(x -> OrganisationId.of(x.id.getId()))
                        .toList());
    }

    public static Organisation asOrganisation(
            OrganisationDbo organisationDbo, Map<Long, Organisation> organisationMap, Map<Long, Country> countryMap) {
        return organisationDbo.asOrganisation(organisationMap, countryMap);
    }

    public static String mapOrdersDomainToDbo(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id.value" -> "id";
            case "name.value" -> "name";
            case "shortName.value" -> "shortName";
            case "type.id" -> "type";
            case "country.name.value" -> "country.name";
            case "childOrganisationIds" -> "childOrganisations";
            default -> order.getProperty();
        };
    }

    public static String mapOrdersDboToDomain(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id" -> "id.value";
            case "name" -> "name.value";
            case "shortName" -> "shortName.value";
            case "type" -> "type.id";
            case "country.name" -> "country.name.value";
            case "childOrganisations" -> "childOrganisationIds";
            default -> order.getProperty();
        };
    }
}
