package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RaceTest {

    // -------------------------------------------------------------------------
    // of() — Fabrikmethoden
    // -------------------------------------------------------------------------

    @Test
    void of_eventIdAndRaceNumber_setsFields() {
        Race race = Race.of(EventId.of(1L), (byte) 2);

        assertThat(race.getEventId()).isEqualTo(EventId.of(1L));
        assertThat(race.getRaceNumber()).isEqualTo(RaceNumber.of((byte) 2));
        assertThat(race.getRaceName().value()).isNull();
        assertThat(race.getId()).isEqualTo(RaceId.empty());
    }

    @Test
    void of_withRaceName_setsAllFields() {
        Race race = Race.of(EventId.of(5L), "Lauf 1", (byte) 1);

        assertThat(race.getEventId()).isEqualTo(EventId.of(5L));
        assertThat(race.getRaceNumber()).isEqualTo(RaceNumber.of((byte) 1));
        assertThat(race.getRaceName().value()).isEqualTo("Lauf 1");
    }

    // -------------------------------------------------------------------------
    // getDomainKey
    // -------------------------------------------------------------------------

    @Test
    void getDomainKey_returnsEventIdAndRaceNumber() {
        Race race = Race.of(EventId.of(3L), (byte) 2);

        Race.DomainKey key = race.getDomainKey();

        assertThat(key.eventId()).isEqualTo(EventId.of(3L));
        assertThat(key.raceNumber()).isEqualTo(RaceNumber.of((byte) 2));
    }

    // -------------------------------------------------------------------------
    // compareTo — Sortierung nach raceNumber → raceName (nulls last) → eventId
    // -------------------------------------------------------------------------

    @Test
    void compareTo_differentRaceNumber_ordersByRaceNumber() {
        Race race1 = Race.of(EventId.of(1L), (byte) 1);
        Race race2 = Race.of(EventId.of(1L), (byte) 2);

        assertThat(race1.compareTo(race2)).isLessThan(0);
        assertThat(race2.compareTo(race1)).isGreaterThan(0);
    }

    @Test
    void compareTo_sameRaceNumber_ordersByRaceNameAlphabetically() {
        Race raceA = Race.of(EventId.of(1L), "Lauf A", (byte) 1);
        Race raceB = Race.of(EventId.of(1L), "Lauf B", (byte) 1);

        assertThat(raceA.compareTo(raceB)).isLessThan(0);
        assertThat(raceB.compareTo(raceA)).isGreaterThan(0);
    }

    @Test
    void compareTo_sameRaceNumber_nullRaceNameComesLast() {
        Race withName = Race.of(EventId.of(1L), "Lauf A", (byte) 1);
        Race withoutName = Race.of(EventId.of(1L), (byte) 1); // raceName = null

        assertThat(withName.compareTo(withoutName)).isLessThan(0);
        assertThat(withoutName.compareTo(withName)).isGreaterThan(0);
    }

    @Test
    void compareTo_sameRaceNumberAndName_ordersByEventId() {
        Race race1 = Race.of(EventId.of(1L), "Lauf", (byte) 1);
        Race race2 = Race.of(EventId.of(2L), "Lauf", (byte) 1);

        assertThat(race1.compareTo(race2)).isLessThan(0);
        assertThat(race2.compareTo(race1)).isGreaterThan(0);
    }

    @Test
    void compareTo_equalRaces_returnsZero() {
        Race race1 = Race.of(EventId.of(1L), "Lauf", (byte) 1);
        Race race2 = Race.of(EventId.of(1L), "Lauf", (byte) 1);

        assertThat(race1.compareTo(race2)).isEqualTo(0);
    }
}
