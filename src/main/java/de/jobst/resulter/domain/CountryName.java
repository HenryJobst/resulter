package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record CountryName(String value) {
    public static CountryName of(String countryName) {
        return new CountryName(countryName);
    }
}
