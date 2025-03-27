package de.jobst.resulter.domain;

import java.util.Objects;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public enum CupType {
    ADD("ADD"),
    NOR("NOR"),
    KJ("KJ"),
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
        return this == KJ || this == NEBEL || this == KRISTALL;
    }
}
