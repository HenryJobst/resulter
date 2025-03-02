package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record CountryCode(String value) {
    public static CountryCode of(String countryCode) {
        return new CountryCode(countryCode);
    }
}
