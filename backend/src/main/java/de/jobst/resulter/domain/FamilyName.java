package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.StringUtils;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record FamilyName(String value) implements Comparable<FamilyName> {

    public static FamilyName of(String value) {
        return new FamilyName(StringUtils.formatAsName(value));
    }

    @Override
    public int compareTo(FamilyName o) {
        return value.compareTo(o.value);
    }
}
