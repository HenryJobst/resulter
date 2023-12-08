package de.jobst.resulter.domain;

public record OrganisationShortName(String shortName) {
    public static OrganisationShortName of(String shortName) {
        return new OrganisationShortName(shortName);
    }
}
