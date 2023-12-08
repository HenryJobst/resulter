package de.jobst.resulter.domain;

public record OrganisationId(Long id) {
    public static OrganisationId of(Long id) {
        return new OrganisationId(id);
    }
}
