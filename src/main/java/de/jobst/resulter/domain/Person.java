package de.jobst.resulter.domain;

import java.time.LocalDate;

public record Person(PersonName personName, BirthDate birthDate, Gender gender) {
    public static Person of(PersonName personName, BirthDate birthDate, Gender gender) {
        return new Person(personName, birthDate, gender);
    }

    public static Person of(FamilyName familyName, GivenName givenName, LocalDate birthDate, Gender gender) {
        return new Person(PersonName.of(familyName, givenName), BirthDate.of(birthDate), gender);
    }

    public static Person of(String familyName, String givenName, LocalDate birthDate, String gender) {
        return new Person(PersonName.of(FamilyName.of(familyName), GivenName.of(givenName)),
                BirthDate.of(birthDate), Gender.of(gender));
    }
}
