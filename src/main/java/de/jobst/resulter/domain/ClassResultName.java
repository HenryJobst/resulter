package de.jobst.resulter.domain;

public record ClassResultName(String value) {
    public static ClassResultName of(String name) {
        return new ClassResultName(name);
    }
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
