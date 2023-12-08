package de.jobst.resulter.domain;

public record FamilyName(String value) {
    public static FamilyName of(String value) {
        return new FamilyName(value);
    }
}
