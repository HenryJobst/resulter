package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ShallowLoadProxy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("FieldMayBeFinal")
@Getter
public class Organisation implements Comparable<Organisation> {

    @NonNull
    @Setter
    private OrganisationId id;

    private OrganisationName name;
    private OrganisationShortName shortName;
    private OrganisationType type;
    private ShallowLoadProxy<Country> country;

    @NonNull
    private ShallowLoadProxy<Organisations> parentOrganisations = ShallowLoadProxy.empty();

    public Organisation(@NonNull OrganisationId id,
                        @NonNull OrganisationName name,
                        @NonNull OrganisationShortName shortName,
                        @NonNull OrganisationType type,
                        @NonNull ShallowLoadProxy<Country> country,
                        @NonNull ShallowLoadProxy<Organisations> parentOrganisations
    ) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.type = type;
        this.country = country;
        this.parentOrganisations = parentOrganisations;
    }

    public static Organisation of(String name, String shortName) {
        return Organisation.of(name, shortName, null);
    }

    public static Organisation of(String name, String shortName, Country country) {
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
                                  @Nullable Collection<Organisation> parentOrganisations) {
        return new Organisation(OrganisationId.of(id),
                OrganisationName.of(name),
                OrganisationShortName.of(shortName),
                OrganisationType.fromValue(type),
                ShallowLoadProxy.of(country),
                (parentOrganisations != null) ?
                        ShallowLoadProxy.of(Organisations.of(parentOrganisations)) :
                        ShallowLoadProxy.empty()
        );
    }

    @Override
    public int compareTo(@NonNull Organisation o) {
        int val = type.compareTo(o.type);
        if (val == 0) {
            val = name.compareTo(o.name);
        }
        return val;
    }
}
