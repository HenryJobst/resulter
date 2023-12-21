package de.jobst.resulter.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

@SuppressWarnings("FieldMayBeFinal")
@Getter
public class Organisation {

    @NonNull
    @Setter
    private OrganisationId id;

    private OrganisationName name;
    private OrganisationShortName shortName;

    public Organisation(@NonNull OrganisationId id,
                        @NonNull OrganisationName name,
                        @NonNull OrganisationShortName shortName) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
    }

    public static Organisation of(String name, String shortName) {
        return new Organisation(OrganisationId.empty(), OrganisationName.of(name), OrganisationShortName.of(shortName));
    }

    public static Organisation of(long id, String name, String shortName) {
        return new Organisation(OrganisationId.of(id), OrganisationName.of(name), OrganisationShortName.of(shortName));
    }
}
