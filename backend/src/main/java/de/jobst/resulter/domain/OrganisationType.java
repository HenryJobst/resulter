package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;

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

    public String value() {
        return value;
    }
}
