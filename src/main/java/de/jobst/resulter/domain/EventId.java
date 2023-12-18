package de.jobst.resulter.domain;

public record EventId(long value) {

    public static EventId of(long value) {
        return new EventId(value);
    }

    public static EventId empty() {
        return new EventId(0L);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
