package de.jobst.resulter.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
public class Organisation {

    @Nullable
    @Setter
    private OrganisationId id;

    OrganisationName name;
    OrganisationShortName shortName;

    public Organisation(@Nullable OrganisationId id,
                        @NonNull OrganisationName name,
                        @NonNull OrganisationShortName shortName) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
    }

    public static Organisation of(String name, String shortName) {
        return new Organisation(null, OrganisationName.of(name), OrganisationShortName.of(shortName));
    }
}
