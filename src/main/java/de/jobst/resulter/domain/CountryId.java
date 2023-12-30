package de.jobst.resulter.domain;

public record CountryId(long value) {

    public static CountryId of(long value) {
        if (value < 0L) {
            throw new IllegalArgumentException("Id must be greater or equal 0.");
        }
        return new CountryId(value);
    }

    public static CountryId empty() {
        return new CountryId(0L);
    }

    public boolean isPersistent() {
        return value != empty().value;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
