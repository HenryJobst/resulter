package de.jobst.resulter.domain;

public record PersonId(long value) {

    public static PersonId of(long value) {
        return new PersonId(value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
