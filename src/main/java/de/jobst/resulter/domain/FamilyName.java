package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.StringUtils;

public record FamilyName(String value) {

    public static FamilyName of(String value) {
        return new FamilyName(StringUtils.formatAsName(value));
    }
}
