package de.jobst.resulter.domain;

public record CountryCode(String value) {
    public static CountryCode of(String countryCode) {
        return new CountryCode(countryCode);
    }
}
