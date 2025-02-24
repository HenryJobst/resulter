package de.jobst.resulter.domain;

import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.util.Objects;

@AggregateRoot
@Getter
public final class Person implements Comparable<Person> {

    @Identity
    @NonNull
    private final PersonId id;

    private final PersonName personName;
    private final BirthDate birthDate;
    private final Gender gender;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Person person)) {
            return false;
        }

        return id.equals(person.id) &&
               Objects.equals(personName, person.personName) &&
               Objects.equals(birthDate, person.birthDate) &&
               Objects.equals(gender, person.gender);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + Objects.hashCode(personName);
        result = 31 * result + Objects.hashCode(birthDate);
        result = 31 * result + Objects.hashCode(gender);
        return result;
    }

    public record DomainKey(PersonName personName, BirthDate birthDate, Gender gender) implements Comparable<DomainKey> {

        @Override
        public int compareTo(@NonNull DomainKey o) {
            int val = personName.compareTo(o.personName);
            if (val == 0) {
                if (birthDate != null && o.birthDate != null) {
                    val = birthDate.compareTo(o.birthDate);
                } else if (birthDate != null || o.birthDate != null) {
                    if (birthDate == null) {
                        val = -1;
                    } else {
                        val = 1;
                    }
                }
            }
            if (val == 0) {
                val = gender.compareTo(o.gender);
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

    public static Person of(FamilyName familyName, GivenName givenName, LocalDate birthDate, Gender gender) {
        return new Person(PersonId.empty(), PersonName.of(familyName, givenName), BirthDate.of(birthDate), gender);
    }

    public static Person of(String familyName, String givenName, LocalDate birthDate, Gender gender) {
        return new Person(PersonId.empty(),
            PersonName.of(FamilyName.of(familyName), GivenName.of(givenName)),
            BirthDate.of(birthDate),
            gender);
    }

    public static Person of(long id, String familyName, String givenName, LocalDate birthDate, Gender gender) {
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
        if (val == 0) {
            if (this.birthDate != null && o.birthDate != null) {
                val = this.birthDate.compareTo(o.birthDate);
            } else {
                if (this.birthDate != null || o.birthDate != null) {
                    if (this.birthDate == null) {
                        val = -1;
                    } else {
                        val = 1;
                    }
                }
            }
        }
        if (val == 0) {
            val = gender.compareTo(o.gender);
        }
        if (val == 0) {
            val = Long.compare(this.id.value(), o.id.value());
        }
        return val;
    }

    public DomainKey getDomainKey() {
        return new DomainKey(personName, birthDate, gender);
    }
}
