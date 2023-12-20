package de.jobst.resulter.domain;

public record PersonId(long value) {

    public static PersonId of(long value) {
        if (value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0.");
        }
        return new PersonId(value);
    }

    public static PersonId empty() {
        return new PersonId(0L);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
