package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

@ValueObject
public record CountryName(@Nullable String value) {
    public static CountryName of(@Nullable String countryName) {
        return new CountryName(countryName);
    }
}
