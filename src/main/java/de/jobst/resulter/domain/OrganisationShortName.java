package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record OrganisationShortName(String value) {
    public static OrganisationShortName of(String shortName) {
        return new OrganisationShortName(shortName);
    }
}
