package de.jobst.resulter.domain;

public record ClassResultId(long value) {

    public static ClassResultId of(long value) {
        return new ClassResultId(value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
