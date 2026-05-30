package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersonRaceResultTest {

    // -------------------------------------------------------------------------
    // compareTo — primäre Sortierung nach raceNumber
    // -------------------------------------------------------------------------

    @Test
    void compareTo_differentRaceNumber_ordersByRaceNumber() {
        PersonRaceResult prr1 = prr("H21", 1L, 1000.0, 1L, (byte) 1);
        PersonRaceResult prr2 = prr("H21", 1L, 1000.0, 1L, (byte) 2);

        assertThat(prr1.compareTo(prr2)).isLessThan(0);
        assertThat(prr2.compareTo(prr1)).isGreaterThan(0);
    }

    // -------------------------------------------------------------------------
    // compareTo — gleiche raceNumber, gleiche Klasse: Sortierung nach Position
    // -------------------------------------------------------------------------

    @Test
    void compareTo_sameRaceNumberAndClass_ordersByPosition() {
        PersonRaceResult first = prr("H21", 1L, 1000.0, 1L, (byte) 1);
        PersonRaceResult second = prr("H21", 2L, 1100.0, 2L, (byte) 1);

        assertThat(first.compareTo(second)).isLessThan(0);
        assertThat(second.compareTo(first)).isGreaterThan(0);
    }

    @Test
    void compareTo_sameRaceNumberAndClassAndPosition_ordersByRuntime() {
        PersonRaceResult faster = prr("H21", 1L, 1000.0, 1L, (byte) 1);
        PersonRaceResult slower = prr("H21", 2L, 1100.0, 1L, (byte) 1); // gleiche Position, unterschiedliche Zeit
        // Pos-Vergleich = 0 → Fallback auf runtime
        assertThat(faster.compareTo(slower)).isLessThan(0);
        assertThat(slower.compareTo(faster)).isGreaterThan(0);
    }

    // -------------------------------------------------------------------------
    // compareTo — gleiche raceNumber, unterschiedliche Klasse: Sortierung nach Runtime
    // -------------------------------------------------------------------------

    @Test
    void compareTo_sameRaceNumberDifferentClass_ordersByRuntime() {
        PersonRaceResult faster = prr("H21", 1L, 1000.0, 1L, (byte) 1);
        PersonRaceResult slower = prr("D21", 2L, 1200.0, 1L, (byte) 1);

        assertThat(faster.compareTo(slower)).isLessThan(0);
        assertThat(slower.compareTo(faster)).isGreaterThan(0);
    }

    // -------------------------------------------------------------------------
    // compareTo — Sortierung nach personId als Tiebreaker
    // -------------------------------------------------------------------------

    @Test
    void compareTo_sameRaceNumberClassPositionRuntime_ordersByPersonId() {
        PersonRaceResult prr1 = prr("H21", 1L, 1000.0, 1L, (byte) 1);
        PersonRaceResult prr2 = prr("H21", 2L, 1000.0, 1L, (byte) 1);

        assertThat(prr1.compareTo(prr2)).isLessThan(0);
        assertThat(prr2.compareTo(prr1)).isGreaterThan(0);
    }

    // -------------------------------------------------------------------------
    // compareTo — Sortierung nach classResultShortName als letzter Tiebreaker
    // -------------------------------------------------------------------------

    @Test
    void compareTo_sameRaceNumberPositionRuntimePersonId_ordersByClassShortName() {
        // Verschiedene Klassen mit identischer Runtime → Step 2 (runtime) = 0 → personId gleich → classShortName
        PersonRaceResult d21 = prr("D21", 1L, 1000.0, 1L, (byte) 1);
        PersonRaceResult h21 = prr("H21", 1L, 1000.0, 1L, (byte) 1);

        assertThat(d21.compareTo(h21)).isLessThan(0); // D < H alphabetisch
        assertThat(h21.compareTo(d21)).isGreaterThan(0);
    }

    // -------------------------------------------------------------------------
    // compareTo — gleiche Objekte
    // -------------------------------------------------------------------------

    @Test
    void compareTo_sameValues_returnsZero() {
        PersonRaceResult prr1 = prr("H21", 1L, 1000.0, 1L, (byte) 1);
        PersonRaceResult prr2 = prr("H21", 1L, 1000.0, 1L, (byte) 1);

        assertThat(prr1.compareTo(prr2)).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // of() — Null-Branches und 9-Parameter-Variante
    // -------------------------------------------------------------------------

    @Test
    void of_nullPersonId_usesEmptyPersonId() {
        PersonRaceResult prr = PersonRaceResult.of("H21", null, null, null, 1000.0, 1L, (byte) 1, ResultStatus.OK);
        assertThat(prr.getPersonId()).isEqualTo(PersonId.empty());
    }

    @Test
    void of_nullResultStatus_usesDIDNotStart() {
        PersonRaceResult prr = PersonRaceResult.of("H21", 1L, null, null, 1000.0, 1L, (byte) 1, null);
        assertThat(prr.getState()).isEqualTo(ResultStatus.DID_NOT_START);
    }

    @Test
    void of_withSplitTimeListId_setsSplitTimeListId() {
        SplitTimeListId stlId = SplitTimeListId.of(99L);
        PersonRaceResult prr = PersonRaceResult.of(
                "H21", 1L, null, null, 1000.0, 1L, ResultStatus.OK, (byte) 1, stlId);
        assertThat(prr.getSplitTimeListId()).isEqualTo(stlId);
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    private static PersonRaceResult prr(String cls, Long personId, Double runtime, Long position, Byte raceNumber) {
        return PersonRaceResult.of(cls, personId, null, null, runtime, position, raceNumber, ResultStatus.OK);
    }
}
