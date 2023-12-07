package de.jobst.resulter.domain;

public record EventId(long value) {

    public static EventId of(long value) {
        return new EventId(value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
