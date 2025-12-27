package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

@ValueObject
public record OrganisationName(String value) implements Comparable<OrganisationName> {
    public static OrganisationName of(String organisationName) {
        return new OrganisationName(organisationName);
    }

    @Override
    public int compareTo(@NonNull OrganisationName o) {
        return value.compareTo(o.value);
    }
}
