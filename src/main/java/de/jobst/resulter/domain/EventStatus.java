package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public enum EventStatus {
    PLANNED("Planned"),
    APPLIED("Applied"),
    PROPOSED("Proposed"),
    SANCTIONED("Sanctioned"),
    CANCELED("Canceled"),
    RESCHEDULED("Rescheduled");
    private final String value;

    EventStatus(String v) {
        value = v;
    }

    public static EventStatus fromValue(String v) {
        for (EventStatus c : EventStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public String value() {
        return value;
    }

    public static EventStatus getDefault() {
        return EventStatus.SANCTIONED;
    }
}
