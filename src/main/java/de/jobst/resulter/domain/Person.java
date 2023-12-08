package de.jobst.resulter.domain;

import java.time.LocalDate;

public record Person(PersonName personName, BirthDate birthDate) {
    public static Person of(PersonName personName, BirthDate birthDate) {
        return new Person(personName, birthDate);
    }

    public static Person of(FamilyName familyName, GivenName givenName, LocalDate birthDate) {
        return new Person(PersonName.of(familyName, givenName), BirthDate.of(birthDate));
    }

    public static Person of(String familyName, String givenName, LocalDate birthDate) {
        return new Person(PersonName.of(FamilyName.of(familyName), GivenName.of(givenName)),
                BirthDate.of(birthDate));
    }
}
