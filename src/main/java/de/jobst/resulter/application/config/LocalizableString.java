package de.jobst.resulter.application.config;

import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;

public record LocalizableString(@NotNull MessageKey messageKey, Map<String, Object> messageParameters) {
    static public LocalizableString of(@NotNull MessageKey messageKey, Map<String, Object> messageParameters) {
        return new LocalizableString(messageKey, messageParameters);
    }
    static public LocalizableString of(@NotNull MessageKey messageKey) {
        return new LocalizableString(messageKey, new HashMap<>());
    }
}
