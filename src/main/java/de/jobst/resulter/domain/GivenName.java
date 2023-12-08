package de.jobst.resulter.domain;

public record GivenName(String value) {
    public static GivenName of(String value) {
        return new GivenName(value);
    }
}
