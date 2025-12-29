package de.jobst.resulter.domain;

import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Association;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.Nullable;

import java.util.*;

@AggregateRoot
@Getter
public final class Organisation implements Comparable<Organisation> {

    @Identity
    private final OrganisationId id;

    private final OrganisationName name;

    private final OrganisationShortName shortName;

    private final OrganisationType type;

    @Association
    @Nullable
    private final CountryId country;

    @Association
    private final Collection<OrganisationId> childOrganisations;

    public Organisation(
            OrganisationId id,
            OrganisationName name,
            OrganisationShortName shortName,
            OrganisationType type,
            @Nullable CountryId country,
            Collection<OrganisationId> childOrganisations) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.type = type;
        this.country = country;
        this.childOrganisations = childOrganisations;
    }

    public static Organisation of(String name, String shortName) {
        return Organisation.of(name, shortName, null);
    }

    public static Organisation of(String name, String shortName, @Nullable CountryId country) {
        return Organisation.of(
                OrganisationId.empty().value(),
                name,
                shortName,
                OrganisationType.OTHER.value(),
                country,
                new ArrayList<>());
    }

    public static Organisation of(@Nullable Long id, String name, String shortName) {
        return Organisation.of(id, name, shortName, OrganisationType.OTHER.value(), null, new ArrayList<>());
    }

    public static Organisation of(
            @Nullable Long id,
            String name,
            String shortName,
            String type,
            @Nullable CountryId country,
            Collection<OrganisationId> childOrganisations) {
        return new Organisation(
                id == null ? OrganisationId.empty() : OrganisationId.of(id),
                OrganisationName.of(name),
                OrganisationShortName.of(shortName),
                OrganisationType.fromValue(type),
                country,
                childOrganisations);
    }

    public static Organisation of(
            OrganisationName name,
            OrganisationShortName shortName,
            OrganisationType type,
            @Nullable CountryId country,
            Collection<OrganisationId> childOrganisations) {
        return new Organisation(OrganisationId.empty(), name, shortName, type, country, childOrganisations);
    }

    @Override
    public int compareTo(Organisation o) {
        int val = type.compareTo(o.type);
        if (val == 0) {
            val = name.compareTo(o.name);
        }
        if (val == 0) {
            if (null != country) {
                if (null != o.country) {
                    val = country.compareTo(o.country);
                } else {
                    val = 1;
                }
            } else if (null != o.country) {
                val = -1;
            }
        }
        return val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Organisation that = (Organisation) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(shortName, that.shortName)
                && Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, shortName, country);
    }

    public boolean containsOrganisationWithShortName(String name, Map<OrganisationId, Organisation> organisationById) {
        if (getShortName().value().equals(name)) {
            return true;
        }
        return getChildOrganisations().stream().anyMatch(subOrg -> {
            Optional<Organisation> subOrganisation = Optional.ofNullable(organisationById.get(subOrg));
            return subOrganisation
                    .map(o -> o.containsOrganisationWithShortName(name, organisationById))
                    .orElse(false);
        });
    }

    public boolean containsOrganisationWithId(OrganisationId id, Map<OrganisationId, Organisation> organisationById) {
        return getId().equals(id)
                || childOrganisations.stream().anyMatch(subOrg -> {
                    Optional<Organisation> subOrganisation = Optional.ofNullable(organisationById.get(subOrg));
                    return subOrganisation
                            .map(o -> o.containsOrganisationWithId(id, organisationById))
                            .orElse(false);
                });
    }
}
