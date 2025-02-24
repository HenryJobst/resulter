package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.Objects;

@ValueObject
public enum CupType {
    ADD("ADD"),
    NOR("NOR"),
    NEBEL("NEBEL"),
    KRISTALL("KRISTALL"),
    ;

    private final String value;

    CupType(String value) {
        this.value = value;
    }

    public static CupType fromValue(String v) {
        for (CupType c : CupType.values()) {
            if (Objects.equals(c.value, v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public String value() {
        return value;
    }

    public boolean isGroupedByOrganisation() {
        // cup results are grouped by organisation
        return this == NEBEL || this == KRISTALL;
    }
}
