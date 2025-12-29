package de.jobst.resulter.domain;

import java.time.LocalDate;
import java.util.Comparator;

import de.jobst.resulter.domain.comparators.PersonComparator;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.Nullable;

@AggregateRoot
public record Person(@Identity PersonId id, PersonName personName, @Nullable BirthDate birthDate, Gender gender)
    implements Comparable<Person> {

    public record DomainKey(PersonName personName, @Nullable BirthDate birthDate, Gender gender) implements Comparable<DomainKey> {

        private static final Comparator<DomainKey> COMPARATOR_DOMAIN_KEY = Comparator.comparing(DomainKey::personName)
            .thenComparing(DomainKey::birthDate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(DomainKey::gender);

        @Override
        public int compareTo(DomainKey o) {
            return COMPARATOR_DOMAIN_KEY.compare(this, o);
        }
    }

    public static Person of(PersonName personName, @Nullable BirthDate birthDate, Gender gender) {
        return new Person(PersonId.empty(), personName, birthDate, gender);
    }

    public static Person of(FamilyName familyName, GivenName givenName, @Nullable LocalDate birthDate, Gender gender) {
        return new Person(PersonId.empty(), PersonName.of(familyName, givenName), BirthDate.of(birthDate), gender);
    }

    public static Person of(String familyName, String givenName, @Nullable LocalDate birthDate, Gender gender) {
        return new Person(PersonId.empty(),
            PersonName.of(FamilyName.of(familyName), GivenName.of(givenName)),
            BirthDate.of(birthDate),
            gender);
    }

    public static Person of(@Nullable Long id, String familyName, String givenName, @Nullable LocalDate birthDate, Gender gender) {
        return new Person(id == null ? PersonId.empty() : PersonId.of(id),
            PersonName.of(FamilyName.of(familyName), GivenName.of(givenName)),
            BirthDate.of(birthDate),
            gender);
    }

    @Override
    public int compareTo(Person o) {
        return PersonComparator.COMPARATOR.compare(this, o);
    }

    public DomainKey getDomainKey() {
        return new DomainKey(personName, birthDate, gender);
    }
}
