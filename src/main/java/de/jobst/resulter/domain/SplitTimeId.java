package de.jobst.resulter.domain;

public record SplitTimeId(long value) {

    public static SplitTimeId of(long value) {
        return new SplitTimeId(value);
    }

    public static SplitTimeId empty() {
        return new SplitTimeId(0L);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
