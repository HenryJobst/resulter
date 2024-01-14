package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ValueObjectChecks;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

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

    @NonNull
    @Setter
    private CountryId countryId;

    @NonNull
    @Setter
    private Collection<OrganisationId> childOrganisationIds;

    public Organisation(@NonNull OrganisationId id,
                        @NonNull OrganisationName name,
                        @NonNull OrganisationShortName shortName,
                        @NonNull OrganisationType type,
                        @NonNull CountryId countryId,
                        @NonNull Collection<OrganisationId> childOrganisationIds) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.type = type;
        this.countryId = countryId;
        this.childOrganisationIds = childOrganisationIds;
    }

    public static Organisation of(String name, String shortName) {
        return Organisation.of(name, shortName, CountryId.empty());
    }

    public static Organisation of(String name, String shortName, @NonNull CountryId countryId) {
        return Organisation.of(OrganisationId.empty().value(),
            name,
            shortName,
            OrganisationType.OTHER.value(),
            countryId,
            new ArrayList<>());
    }

    public static Organisation of(long id, String name, String shortName) {
        return Organisation.of(id,
            name,
            shortName,
            OrganisationType.OTHER.value(),
            CountryId.empty(),
            new ArrayList<>());
    }

    public static Organisation of(long id,
                                  @NonNull String name,
                                  @NonNull String shortName,
                                  @NonNull String type,
                                  @NonNull CountryId countryId,
                                  @NonNull Collection<OrganisationId> childOrganisationIds) {
        return new Organisation(OrganisationId.of(id),
            OrganisationName.of(name),
            OrganisationShortName.of(shortName),
            OrganisationType.fromValue(type),
            countryId,
            childOrganisationIds);
    }

    public static Organisation of(@NonNull OrganisationName name,
                                  @NonNull OrganisationShortName shortName,
                                  @NonNull OrganisationType type,
                                  @NonNull CountryId countryId,
                                  @NonNull Collection<OrganisationId> childOrganisationIds) {
        return new Organisation(OrganisationId.empty(), name, shortName, type, countryId, childOrganisationIds);
    }

    @Override
    public int compareTo(@NonNull Organisation o) {
        int val = type.compareTo(o.type);
        if (val == 0) {
            val = name.compareTo(o.name);
        }
        return val;
    }

    public void update(@NonNull OrganisationName name,
                       @NonNull OrganisationShortName shortName,
                       @NonNull OrganisationType type,
                       @NonNull CountryId countryId,
                       @NonNull Collection<OrganisationId> childOrganisationIds) {
        ValueObjectChecks.requireNotNull(name);
        this.name = name;
        this.shortName = shortName;
        this.type = type;
        this.countryId = countryId;
        this.childOrganisationIds = childOrganisationIds;
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

    /*
    public boolean containsOrganisationWithName(String name) {
        if (getName().value().equals(name)) {
            return true;
        }
        return parentOrganisationIds.stream()
                .anyMatch(subOrg -> subOrg.containsOrganisationWithName(name));
    }
    */

}
