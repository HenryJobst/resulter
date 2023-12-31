package de.jobst.resulter.domain;

public record CupId(long value) {

    public static CupId of(long value) {
        if (value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0.");
        }
        return new CupId(value);
    }

    public static CupId empty() {
        return new CupId(0L);
    }

    public boolean isPersistent() {
        return value != empty().value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
