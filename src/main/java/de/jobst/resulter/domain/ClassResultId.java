package de.jobst.resulter.domain;

public record ClassResultId(long value) {

    public static ClassResultId of(long value) {
        return new ClassResultId(value);
    }

    public static ClassResultId empty() {
        return new ClassResultId(0L);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
