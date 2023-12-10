package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

import java.text.MessageFormat;

public enum Gender {
    M("M"),
    F("F"),
    ;
    final String value;

    Gender(@NonNull String value) {
        this.value = value;
    }

    public static Gender of(@NonNull String value) {
        return switch (value) {
            case "M" -> M;
            case "F" -> F;
            default -> throw new IllegalStateException(MessageFormat.format("Unexpected value: {0}", value));
        };
    }
}
