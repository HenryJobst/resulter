package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PersonTest {

    // -------------------------------------------------------------------------
    // of() — Fabrikmethoden
    // -------------------------------------------------------------------------

    @Test
    void of_stringsAndBirthDate_setsAllFields() {
        Person person = Person.of("Mustermann", "Max", LocalDate.of(1990, 5, 15), Gender.M);

        assertThat(person.personName().familyName().value()).isEqualTo("Mustermann");
        assertThat(person.personName().givenName().value()).isEqualTo("Max");
        assertThat(person.birthDate().value()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(person.gender()).isEqualTo(Gender.M);
        assertThat(person.id()).isEqualTo(PersonId.empty());
    }

    @Test
    void of_withId_setsId() {
        Person person = Person.of(42L, "Mustermann", "Max", null, Gender.F);

        assertThat(person.id()).isEqualTo(PersonId.of(42L));
    }

    // -------------------------------------------------------------------------
    // compareTo — PersonComparator: familyName → givenName → birthDate → gender → id
    // -------------------------------------------------------------------------

    @Test
    void compareTo_ordersByFamilyNameFirst() {
        Person abt = Person.of("Abt", "Hans", null, Gender.M);
        Person schmidt = Person.of("Schmidt", "Hans", null, Gender.M);

        assertThat(abt.compareTo(schmidt)).isLessThan(0);
        assertThat(schmidt.compareTo(abt)).isGreaterThan(0);
    }

    @Test
    void compareTo_sameFamilyName_ordersByGivenName() {
        Person anna = Person.of("Müller", "Anna", null, Gender.F);
        Person bert = Person.of("Müller", "Bert", null, Gender.M);

        assertThat(anna.compareTo(bert)).isLessThan(0);
        assertThat(bert.compareTo(anna)).isGreaterThan(0);
    }

    @Test
    void compareTo_sameName_earlierBirthDateComesFirst() {
        LocalDate earlier = LocalDate.of(1980, 1, 1);
        LocalDate later = LocalDate.of(1990, 1, 1);

        Person older = Person.of("Müller", "Anna", earlier, Gender.F);
        Person younger = Person.of("Müller", "Anna", later, Gender.F);

        assertThat(older.compareTo(younger)).isLessThan(0);
        assertThat(younger.compareTo(older)).isGreaterThan(0);
    }

    @Test
    void compareTo_sameNameAndBirthDate_ordersByGender() {
        // Gleiches, echtes Geburtsdatum → BirthDate-Vergleich = 0 → weiter zu Gender
        // Enum-Ordinalordnung: M(0) < F(1)
        LocalDate dob = LocalDate.of(1990, 1, 1);
        Person male = Person.of("Müller", "Chris", dob, Gender.M);
        Person female = Person.of("Müller", "Chris", dob, Gender.F);

        assertThat(male.compareTo(female)).isLessThan(0);
        assertThat(female.compareTo(male)).isGreaterThan(0);
    }

    @Test
    void compareTo_equalPersons_returnsZero() {
        Person p1 = Person.of("Müller", "Anna", LocalDate.of(1990, 1, 1), Gender.F);
        Person p2 = Person.of("Müller", "Anna", LocalDate.of(1990, 1, 1), Gender.F);

        assertThat(p1.compareTo(p2)).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // getDomainKey
    // -------------------------------------------------------------------------

    @Test
    void getDomainKey_returnsPersonNameBirthDateGender() {
        LocalDate dob = LocalDate.of(1985, 6, 20);
        Person person = Person.of("Mustermann", "Max", dob, Gender.M);

        Person.DomainKey key = person.getDomainKey();

        assertThat(key.personName().familyName().value()).isEqualTo("Mustermann");
        assertThat(key.birthDate().value()).isEqualTo(dob);
        assertThat(key.gender()).isEqualTo(Gender.M);
    }

    @Test
    void domainKey_compareTo_ordersByName() {
        Person.DomainKey a = new Person.DomainKey(PersonName.of("Abt", "Hans"), null, Gender.M);
        Person.DomainKey b = new Person.DomainKey(PersonName.of("Zander", "Hans"), null, Gender.M);

        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(a)).isGreaterThan(0);
        assertThat(a.compareTo(a)).isEqualTo(0);
    }

    @Test
    void domainKey_compareTo_nullBirthDateSortsLast() {
        LocalDate dob = LocalDate.of(1990, 1, 1);
        Person.DomainKey withDate = new Person.DomainKey(PersonName.of("X", "Y"), BirthDate.of(dob), Gender.M);
        Person.DomainKey withNull = new Person.DomainKey(PersonName.of("X", "Y"), null, Gender.M);

        assertThat(withDate.compareTo(withNull)).isLessThan(0);
    }

    @Test
    void of_personNameBirthDateGender_setsAllFields() {
        PersonName name = PersonName.of("Müller", "Hans");
        BirthDate bd = BirthDate.of(LocalDate.of(1985, 3, 10));

        Person person = Person.of(name, bd, Gender.M);

        assertThat(person.personName()).isEqualTo(name);
        assertThat(person.birthDate()).isEqualTo(bd);
        assertThat(person.gender()).isEqualTo(Gender.M);
    }

    @Test
    void of_familyNameGivenNameBirthDateGender_setsAllFields() {
        FamilyName family = FamilyName.of("Schmidt");
        GivenName given = GivenName.of("Anna");

        Person person = Person.of(family, given, LocalDate.of(1992, 7, 1), Gender.F);

        assertThat(person.personName().familyName().value()).isEqualTo("Schmidt");
        assertThat(person.personName().givenName().value()).isEqualTo("Anna");
    }

    @Test
    void of_withNullId_usesEmptyPersonId() {
        Person person = Person.of(null, "Müller", "Max", null, Gender.M);

        assertThat(person.id()).isEqualTo(PersonId.empty());
    }
}
