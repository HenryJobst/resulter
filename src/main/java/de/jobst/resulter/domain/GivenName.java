package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.StringUtils;

public record GivenName(String value) {
    public static GivenName of(String value) {
        return new GivenName(StringUtils.formatAsName(value));
    }
}
