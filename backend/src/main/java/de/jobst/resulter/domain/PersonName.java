package de.jobst.resulter.domain;

import java.text.MessageFormat;
import java.util.Comparator;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record PersonName(FamilyName familyName, GivenName givenName) implements Comparable<PersonName> {

    @SuppressWarnings("unused")
    public String getFullName() {
        return MessageFormat.format("{1} {0}", familyName.value(), givenName.value());
    }

    public static PersonName of(FamilyName familyName, GivenName givenName) {
        return new PersonName(familyName, givenName);
    }

    public static PersonName of(String familyName, String givenName) {
        return new PersonName(FamilyName.of(familyName), GivenName.of(givenName));
    }

    private static final Comparator<PersonName> COMPARATOR =
        Comparator.comparing(PersonName::familyName)
            .thenComparing(PersonName::givenName);

    @Override
    public int compareTo(PersonName o) {
        return COMPARATOR.compare(this, o);
    }

}
