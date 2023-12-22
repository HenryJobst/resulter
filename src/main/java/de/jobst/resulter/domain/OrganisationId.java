package de.jobst.resulter.domain;

public record OrganisationId(long value) {
    public static OrganisationId of(long value) {
        if (value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0.");
        }
        return new OrganisationId(value);
    }

    public static OrganisationId empty() {
        return new OrganisationId(0L);
    }

    public boolean isPersistent() {
        return value != empty().value;
    }
}
