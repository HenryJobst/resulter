package de.jobst.resulter.domain;

public record PersonResultId(long value) {

    public static PersonResultId of(long value) {
        return new PersonResultId(value);
    }

    public static PersonResultId empty() {
        return new PersonResultId(0L);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
