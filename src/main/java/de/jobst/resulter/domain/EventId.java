package de.jobst.resulter.domain;

public record EventId(long value) {

    public static EventId of(long value) {
        if (value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0.");
        }
        return new EventId(value);
    }

    public static EventId empty() {
        return new EventId(0L);
    }

    public boolean isPersistent() {
        return value != empty().value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
