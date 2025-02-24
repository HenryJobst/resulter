package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Objects;

@ValueObject
public enum OrganisationType {
    IOF("IOF"),
    IOF_REGION("IOFRegion"),
    NATIONAL_FEDERATION("NationalFederation"),
    NATIONAL_REGION("NationalRegion"),
    CLUB("Club"),
    SCHOOL("School"),
    COMPANY("Company"),
    MILITARY("Military"),
    OTHER("Other");

    private final String value;

    OrganisationType(String value) {
        this.value = value;
    }

    public static OrganisationType fromValue(String v) {
        for (OrganisationType c : OrganisationType.values()) {
            if (Objects.equals(c.value, v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public String value() {
        return value;
    }
}
