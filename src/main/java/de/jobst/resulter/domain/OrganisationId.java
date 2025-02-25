package de.jobst.resulter.domain;

import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record OrganisationId(Long value) {

    public static OrganisationId of(Long value) {
        if (value != null && value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0 or null.");
        }
        return new OrganisationId(value);
    }

    public static OrganisationId empty() {
        return new OrganisationId(0L);
    }

    public boolean isPersistent() {
        return !Objects.equals(value, empty().value);
    }
}
