package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

@ValueObject
public enum Gender {
    M("M"),
    F("F"),
    B("B");
    final String value;

    Gender(@NonNull String value) {
        this.value = value;
    }

    public static Gender of(@NonNull String value) {
        return switch (value) {
            case "M" -> M;
            case "F" -> F;
            default -> B;
        };
    }
}
