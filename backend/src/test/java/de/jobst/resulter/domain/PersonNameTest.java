package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersonNameTest {

    @Test
    void of_stringsCreatesName() {
        PersonName pn = PersonName.of("Müller", "Hans");
        assertThat(pn.familyName().value()).isEqualTo("Müller");
        assertThat(pn.givenName().value()).isEqualTo("Hans");
    }

    @Test
    void of_valueObjectsCreatesName() {
        PersonName pn = PersonName.of(FamilyName.of("Schmid"), GivenName.of("Anna"));
        assertThat(pn.familyName().value()).isEqualTo("Schmid");
        assertThat(pn.givenName().value()).isEqualTo("Anna");
    }

    @Test
    void getFullName_returnsGivenNameSpaceFamilyName() {
        PersonName pn = PersonName.of("Müller", "Hans");
        assertThat(pn.getFullName()).isEqualTo("Hans Müller");
    }

    @Test
    void compareTo_ordersByFamilyName() {
        PersonName a = PersonName.of("Aigner", "X");
        PersonName b = PersonName.of("Bauer", "X");
        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(a)).isGreaterThan(0);
    }

    @Test
    void compareTo_sameFamilyName_ordersByGivenName() {
        PersonName a = PersonName.of("Müller", "Anna");
        PersonName b = PersonName.of("Müller", "Zara");
        assertThat(a.compareTo(b)).isLessThan(0);
    }

    @Test
    void compareTo_equalNames_returnsZero() {
        PersonName a = PersonName.of("Müller", "Hans");
        PersonName b = PersonName.of("Müller", "Hans");
        assertThat(a.compareTo(b)).isEqualTo(0);
    }
}
