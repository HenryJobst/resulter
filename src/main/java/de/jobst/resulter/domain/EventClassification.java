package de.jobst.resulter.domain;

public enum EventClassification {

    INTERNATIONAL("International"),
    NATIONAL("National"),
    REGIONAL("Regional"),
    LOCAL("Local"),
    CLUB("Club");
    private final String value;

    EventClassification(String v) {
        value = v;
    }

    public static EventClassification fromValue(String v) {
        for (EventClassification c : EventClassification.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public String value() {
        return value;
    }
}
