package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@ValueObject
public enum Gender {
    M("M"),
    F("F"),
    B("B"),
    U("U");
    final String value;

    Gender(@NonNull String value) {
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
