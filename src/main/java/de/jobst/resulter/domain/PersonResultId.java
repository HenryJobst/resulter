package de.jobst.resulter.domain;

public record PersonResultId(long value) {

    public static PersonResultId of(long value) {
        if (value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0.");
        }
        return new PersonResultId(value);
    }

    public static PersonResultId empty() {
        return new PersonResultId(0L);
    }

    public boolean isPersistent() {
        return value != empty().value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
