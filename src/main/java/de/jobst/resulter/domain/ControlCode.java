package de.jobst.resulter.domain;

public record ControlCode(String value) {
    public static ControlCode of(String value) {
        return new ControlCode(value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
