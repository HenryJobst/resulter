package de.jobst.resulter.domain;

public record Country(CountryId id, String code, String name) {
    public static Country of(String code, String name) {
        return Country.of(CountryId.empty(), code, name);
    }

    public static Country of(CountryId id, String code, String name) {
        return new Country(id, code, name);
    }
}
