package de.jobst.resulter.domain;

public record OrganisationShortName(String value) {
    public static OrganisationShortName of(String shortName) {
        return new OrganisationShortName(shortName);
    }
}
