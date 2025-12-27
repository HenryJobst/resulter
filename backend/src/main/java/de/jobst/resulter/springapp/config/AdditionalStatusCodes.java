package de.jobst.resulter.springapp.config;

import org.jspecify.annotations.Nullable;

import java.text.MessageFormat;

public enum AdditionalStatusCodes {
    UNEXPECTED(1001);

    private static final AdditionalStatusCodes[] VALUES = values();
    private final int value;

    AdditionalStatusCodes(int value) {
        this.value = value;
    }

    public static AdditionalStatusCodes valueOf(int statusCode) {
        AdditionalStatusCodes status = resolve(statusCode);
        if (status == null) {
            throw new IllegalArgumentException(MessageFormat.format("No matching constant for [{0}]", statusCode));
        } else {
            return status;
        }
    }

    @Nullable
    public static AdditionalStatusCodes resolve(int statusCode) {
        for (AdditionalStatusCodes status : VALUES) {
            if (status.value == statusCode) {
                return status;
            }
        }

        return null;
    }

    public int value() {
        return this.value;
    }
}
