package de.jobst.resulter.domain;

public record CountryName(String value) {
    public static CountryName of(String countryName) {
        return new CountryName(countryName);
    }
}
