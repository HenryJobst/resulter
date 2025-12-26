package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

@ValueObject
public enum Gender {
    M("M"),
    F("F"),
    B("B"),
    U("U");
    final String value;

    Gender(String value) {
        this.value = value;
    }

    public static Gender of(@Nullable String value) {
        try {
            if (null == value || value.isBlank()) {
                return U;
            }
            return Gender.valueOf(value);
        } catch (IllegalArgumentException e) {
            return U;
        }
    }
}
