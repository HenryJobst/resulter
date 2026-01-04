package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public enum Discipline {
    SPRINT("Sprint"),
    MIDDLE("Middle"),
    LONG("Long"),
    ULTRALONG("Ultralong"),
    OTHER("Other");
    private final String value;

    Discipline(String v) {
        value = v;
    }

    public static Discipline fromValue(String v) {
        for (Discipline c : Discipline.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public String value() {
        return value;
    }

    public static Discipline getDefault() {
        return Discipline.LONG;
    }
}
