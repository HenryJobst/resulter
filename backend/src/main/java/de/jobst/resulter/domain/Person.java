package de.jobst.resulter.domain;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;
import lombok.Getter;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("ClassCanBeRecord")
@AggregateRoot
@Getter
public final class Person implements Comparable<Person> {

    @Identity
    private final PersonId id;

    private final PersonName personName;
    @Nullable
    private final BirthDate birthDate;
    private final Gender gender;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Person person)) {
            return false;
        }

        return id.equals(person.id)
                && Objects.equals(personName, person.personName)
                && Objects.equals(birthDate, person.birthDate)
                && Objects.equals(gender, person.gender);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + Objects.hashCode(personName);
        result = 31 * result + Objects.hashCode(birthDate);
        result = 31 * result + Objects.hashCode(gender);
        return result;
    }

    public record DomainKey(PersonName personName, @Nullable BirthDate birthDate, Gender gender)
            implements Comparable<DomainKey> {

        @Override
        public int compareTo(DomainKey o) {
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

    public Person(PersonId id, PersonName personName, @Nullable BirthDate birthDate, Gender gender) {
        this.id = id;
        this.personName = personName;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public static Person of(PersonName personName, @Nullable BirthDate birthDate, Gender gender) {
        return new Person(PersonId.empty(), personName, birthDate, gender);
    }

    public static Person of(FamilyName familyName, GivenName givenName, @Nullable LocalDate birthDate, Gender gender) {
        return new Person(PersonId.empty(), PersonName.of(familyName, givenName), BirthDate.of(birthDate), gender);
    }

    public static Person of(String familyName, String givenName, @Nullable LocalDate birthDate, Gender gender) {
        return new Person(
                PersonId.empty(),
                PersonName.of(FamilyName.of(familyName), GivenName.of(givenName)),
                BirthDate.of(birthDate),
                gender);
    }

    public static Person of(long id, String familyName, String givenName, @Nullable LocalDate birthDate, Gender gender) {
        return new Person(
                PersonId.of(id),
                PersonName.of(FamilyName.of(familyName), GivenName.of(givenName)),
                BirthDate.of(birthDate),
                gender);
    }

    private static final Comparator<Person> COMPARATOR =
        Comparator.comparing(Person::getPersonName)
            .thenComparing(Person::getBirthDate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Person::getGender)
            .thenComparing(Person::getId);

    @Override
    public int compareTo(Person o) {
        return COMPARATOR.compare(this, o);
    }

    public DomainKey getDomainKey() {
        return new DomainKey(personName, birthDate, gender);
    }
}
