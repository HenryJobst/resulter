package de.jobst.resulter.domain;

public record PersonName(FamilyName familyName, GivenName givenName) {
    public static PersonName of(FamilyName familyName, GivenName givenName) {
        return new PersonName(familyName, givenName);
    }
    public static PersonName of(String familyName, String givenName) {
        return new PersonName(FamilyName.of(familyName), GivenName.of(givenName));
    }
}
