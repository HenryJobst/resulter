package de.jobst.resulter.domain;

public record OrganisationName(String value) {
    public static OrganisationName of(String organisationName) {
        return new OrganisationName(organisationName);
    }
}
