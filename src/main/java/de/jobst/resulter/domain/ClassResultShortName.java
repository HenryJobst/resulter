package de.jobst.resulter.domain;

public record ClassResultShortName(String value) {
    public static ClassResultShortName of(String name) {
        return new ClassResultShortName(name);
    }
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "=" + value;
    }
}
