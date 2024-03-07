package de.jobst.resulter.domain.util;

import org.apache.commons.lang3.StringUtils;

public class ValueObjectChecks {

    public static void requireNotNull(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Value must be not empty!");
        }
    }

    public static void requireNotEmpty(String value) {
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("String value must be not empty!");
        }
    }

    public static void requireGreaterZero(Long value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Long value must be greater zero!");
        }
    }
}
