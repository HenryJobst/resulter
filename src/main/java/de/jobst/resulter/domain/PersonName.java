package de.jobst.resulter.domain;

import java.text.MessageFormat;

public record PersonName(FamilyName familyName, GivenName givenName) {

    public String getFullName() {
        return MessageFormat.format("{1} {0}", familyName.value(), givenName.value());
    }

    public static PersonName of(FamilyName familyName, GivenName givenName) {
        return new PersonName(familyName, givenName);
    }

    public static PersonName of(String familyName, String givenName) {
        return new PersonName(FamilyName.of(familyName), GivenName.of(givenName));
    }
}
