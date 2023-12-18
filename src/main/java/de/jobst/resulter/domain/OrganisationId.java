package de.jobst.resulter.domain;

public record OrganisationId(Long value) {
    public static OrganisationId of(Long id) {
        return new OrganisationId(id);
    }

    public static OrganisationId empty() {
        return new OrganisationId(0L);
    }
}
