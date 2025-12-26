package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.StringUtils;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record GivenName(String value) implements Comparable<GivenName> {
    public static GivenName of(String value) {
        return new GivenName(StringUtils.formatAsName(value));
    }

    @Override
    public int compareTo(GivenName o) {
        return value.compareTo(o.value);
    }
}
