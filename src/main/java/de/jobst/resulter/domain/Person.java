package de.jobst.resulter.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.time.ZonedDateTime;

@SuppressWarnings("FieldMayBeFinal")
@Getter
public class Person implements Comparable<Person> {

    @NonNull
    @Setter
    private PersonId id;

    private PersonName personName;
    private BirthDate birthDate;
    private Gender gender;

    public record DomainKey(String familyName, String givenName, ZonedDateTime birthday)
        implements Comparable<DomainKey> {

        @Override
        public int compareTo(@NonNull DomainKey o) {
            int val = familyName.compareTo(o.familyName);
            if (val == 0) {
                val = givenName.compareTo(o.givenName);
            }
            if (val == 0) {
                val = birthday.compareTo(o.birthday);
            }
            return val;
        }
    }

    public Person(@NonNull PersonId id, PersonName personName, BirthDate birthDate, Gender gender) {
        this.id = id;
        this.personName = personName;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public static Person of(PersonName personName, BirthDate birthDate, Gender gender) {
        return new Person(PersonId.empty(), personName, birthDate, gender);
    }

    public static Person of(FamilyName familyName, GivenName givenName, ZonedDateTime birthDate, Gender gender) {
        return new Person(PersonId.empty(), PersonName.of(familyName, givenName), BirthDate.of(birthDate), gender);
    }

    public static Person of(String familyName, String givenName, ZonedDateTime birthDate, Gender gender) {
        return new Person(PersonId.empty(),
            PersonName.of(FamilyName.of(familyName), GivenName.of(givenName)),
            BirthDate.of(birthDate),
            gender);
    }

    public static Person of(long id, String familyName, String givenName, ZonedDateTime birthDate, Gender gender) {
        return new Person(PersonId.of(id),
            PersonName.of(FamilyName.of(familyName), GivenName.of(givenName)),
            BirthDate.of(birthDate),
            gender);
    }

    @Override
    public int compareTo(@NonNull Person o) {
        int val = this.personName.familyName().compareTo(o.personName.familyName());
        if (val == 0) {
            val = this.personName.givenName().compareTo(o.personName.givenName());
        }
        return val;
    }

    public DomainKey getDomainKey() {
        return new DomainKey(personName.familyName().value(), personName.givenName().value(), birthDate.value());
    }
}
