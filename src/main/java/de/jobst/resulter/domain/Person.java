package de.jobst.resulter.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

@Getter
public class Person {

    @Nullable
    @Setter
    private PersonId id;

    private PersonName personName;
    private BirthDate birthDate;
    private Gender gender;

    public Person(@Nullable PersonId id, PersonName personName, BirthDate birthDate, Gender gender) {
        this.id = id;
        this.personName = personName;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public static Person of(PersonName personName, BirthDate birthDate, Gender gender) {
        return new Person(null, personName, birthDate, gender);
    }

    public static Person of(FamilyName familyName, GivenName givenName, LocalDate birthDate, Gender gender) {
        return new Person(null, PersonName.of(familyName, givenName), BirthDate.of(birthDate), gender);
    }

    public static Person of(String familyName, String givenName, LocalDate birthDate, Gender gender) {
        return new Person(null, PersonName.of(FamilyName.of(familyName), GivenName.of(givenName)),
                BirthDate.of(birthDate), gender);
    }

    public static Person of(long id, String familyName, String givenName, LocalDate birthDate, Gender gender) {
        return new Person(PersonId.of(id), PersonName.of(FamilyName.of(familyName), GivenName.of(givenName)),
                BirthDate.of(birthDate), gender);
    }
}
