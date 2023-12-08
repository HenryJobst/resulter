package de.jobst.resulter.domain;

import java.text.MessageFormat;

public enum Gender {
    M("M"),
    F("F"),
    ;
  String value;

    Gender(String value) {
        this.value = value;
    }

    public static Gender of(String value) {
        return switch (value) {
            case "M" -> M;
            case "F" -> F;
            default -> throw new IllegalStateException(MessageFormat.format("Unexpected value: {0}", value));
        };
    }
}
