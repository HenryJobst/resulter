package de.jobst.resulter.domain;

public record ClassResultId(long value) {

    public static ClassResultId of(long value) {
        if (value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0.");
        }
        return new ClassResultId(value);
    }

    public static ClassResultId empty() {
        return new ClassResultId(0L);
    }

    public boolean isPersistent() {
        return value != empty().value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }

}
