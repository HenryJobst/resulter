package de.jobst.resulter.domain;

public enum EventClassStatus {

    NORMAL("Normal"),
    DIVIDED("Divided"),
    JOINED("Joined"),
    INVALIDATED("Invalidated"),
    INVALIDATED_NO_FEE("InvalidatedNoFee");
    private final String value;

    EventClassStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EventClassStatus fromValue(String v) {
        for (EventClassStatus c : EventClassStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
