package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Getter
public class Organisation implements Comparable<Organisation> {

    @NonNull
    @Setter
    private OrganisationId id;

    @NonNull
    private OrganisationName name;
    private OrganisationShortName shortName;

    @NonNull
    private OrganisationType type;

    @Nullable
    @Setter
    private Country country;

    @NonNull
    @Setter
    private Collection<Organisation> childOrganisations;

    public Organisation(@NonNull OrganisationId id,
                        @NonNull OrganisationName name,
                        @NonNull OrganisationShortName shortName,
                        @NonNull OrganisationType type,
                        @Nullable Country country,
                        @NonNull Collection<Organisation> childOrganisations) {
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

    public static Organisation of(String name, String shortName, @Nullable Country country) {
        return Organisation.of(OrganisationId.empty().value(),
            name,
            shortName,
            OrganisationType.OTHER.value(),
            country,
            new ArrayList<>());
    }

    public static Organisation of(long id, String name, String shortName) {
        return Organisation.of(id, name, shortName, OrganisationType.OTHER.value(), null, new ArrayList<>());
    }

    public static Organisation of(long id,
                                  @NonNull String name,
                                  @NonNull String shortName,
                                  @NonNull String type,
                                  @Nullable Country country,
                                  @NonNull Collection<Organisation> childOrganisations) {
        return new Organisation(OrganisationId.of(id),
            OrganisationName.of(name),
            OrganisationShortName.of(shortName),
            OrganisationType.fromValue(type),
            country,
            childOrganisations);
    }

    public static Organisation of(@NonNull OrganisationName name,
                                  @NonNull OrganisationShortName shortName,
                                  @NonNull OrganisationType type,
                                  @Nullable Country country,
                                  @NonNull Collection<Organisation> childOrganisations) {
        return new Organisation(OrganisationId.empty(), name, shortName, type, country, childOrganisations);
    }

    @Override
    public int compareTo(@NonNull Organisation o) {
        int val = type.compareTo(o.type);
        if (val == 0) {
            val = name.compareTo(o.name);
        }
        if (val == 0) {
            if (null != country) {
                if (null != o.country) {
                    val = country.getName().value().compareTo(o.country.getName().value());
                } else {
                    val = 1;
                }
            } else if (null != o.country) {
                val = -1;
            }
        }
        return val;
    }

    public void update(@NonNull OrganisationName name,
                       @NonNull OrganisationShortName shortName,
                       @NonNull OrganisationType type,
                       @NonNull Country country,
                       @NonNull Collection<Organisation> childOrganisations) {
        ValueObjectChecks.requireNotNull(name);
        this.name = name;
        this.shortName = shortName;
        this.type = type;
        this.country = country;
        this.childOrganisations = childOrganisations;
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
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    public boolean containsOrganisationWithShortName(String name) {
        if (getShortName().value().equals(name)) {
            return true;
        }
        return getChildOrganisations().stream().anyMatch(subOrg -> subOrg.containsOrganisationWithShortName(name));
    }

    public boolean containsOrganisationWithId(OrganisationId id) {
        return getId().equals(id) ||
               childOrganisations.stream().anyMatch(subOrg -> subOrg.containsOrganisationWithId(id));
    }
}
