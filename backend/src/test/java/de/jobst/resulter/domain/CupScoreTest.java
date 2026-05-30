package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CupScoreTest {

    // -------------------------------------------------------------------------
    // compareTo — score → personId → classResultShortName
    // -------------------------------------------------------------------------

    @Test
    void compareTo_ordersByScoreAscending() {
        CupScore lower = score(1L, "H21", 8.0);
        CupScore higher = score(1L, "H21", 12.0);

        assertThat(lower.compareTo(higher)).isLessThan(0);
        assertThat(higher.compareTo(lower)).isGreaterThan(0);
    }

    @Test
    void compareTo_sameScore_ordersByPersonId() {
        CupScore p1 = score(1L, "H21", 10.0);
        CupScore p2 = score(2L, "H21", 10.0);

        assertThat(p1.compareTo(p2)).isLessThan(0);
        assertThat(p2.compareTo(p1)).isGreaterThan(0);
    }

    @Test
    void compareTo_sameScoreAndPersonId_ordersByClassShortName() {
        CupScore d21 = score("D21", 1L, 10.0);
        CupScore h21 = score("H21", 1L, 10.0);

        assertThat(d21.compareTo(h21)).isLessThan(0); // D < H
        assertThat(h21.compareTo(d21)).isGreaterThan(0);
    }

    @Test
    void compareTo_equalValues_returnsZero() {
        CupScore s1 = score(1L, "H21", 10.0);
        CupScore s2 = score(1L, "H21", 10.0);

        assertThat(s1.compareTo(s2)).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    private static CupScore score(long personId, String cls, double points) {
        return new CupScore(PersonId.of(personId), OrganisationId.of(1L), ClassResultShortName.of(cls), points);
    }

    private static CupScore score(String cls, long personId, double points) {
        return new CupScore(PersonId.of(personId), OrganisationId.of(1L), ClassResultShortName.of(cls), points);
    }
}
