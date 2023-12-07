package de.jobst.resulter.domain;

public record EventName(String value) {
    public static EventName of(String name) {
        return new EventName(name);
    }
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
