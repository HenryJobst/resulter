package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PersonResultTest {

    private static PersonResult result(long personId, String cls) {
        return PersonResult.of(
                ClassResultShortName.of(cls),
                PersonId.of(personId),
                null,
                List.of()
        );
    }

    @Test
    void of_setsFieldsCorrectly() {
        PersonResult pr = result(1L, "H21");

        assertThat(pr.personId()).isEqualTo(PersonId.of(1L));
        assertThat(pr.classResultShortName()).isEqualTo(ClassResultShortName.of("H21"));
        assertThat(pr.organisationId()).isNull();
    }

    @Test
    void compareTo_ordersByPersonIdFirst() {
        PersonResult p1 = result(1L, "H21");
        PersonResult p2 = result(2L, "H21");

        assertThat(p1.compareTo(p2)).isLessThan(0);
        assertThat(p2.compareTo(p1)).isGreaterThan(0);
    }

    @Test
    void compareTo_samePersonId_ordersByClassName() {
        PersonResult h21 = result(1L, "H21");
        PersonResult d21 = result(1L, "D21");

        assertThat(d21.compareTo(h21)).isLessThan(0); // D < H
        assertThat(h21.compareTo(d21)).isGreaterThan(0);
    }

    @Test
    void compareTo_equalPersonIdAndClass_returnsZero() {
        PersonResult p1 = result(5L, "H21");
        PersonResult p2 = result(5L, "H21");

        assertThat(p1.compareTo(p2)).isEqualTo(0);
    }
}
