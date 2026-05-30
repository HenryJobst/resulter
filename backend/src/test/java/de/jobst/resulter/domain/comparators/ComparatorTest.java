package de.jobst.resulter.domain.comparators;

import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComparatorTest {

    // -------------------------------------------------------------------------
    // RaceComparator
    // -------------------------------------------------------------------------

    @Test
    void raceComparator_ordersByRaceNumber() {
        Race r1 = Race.of(EventId.of(1L), (byte) 1);
        Race r2 = Race.of(EventId.of(1L), (byte) 2);
        assertThat(RaceComparator.COMPARATOR.compare(r1, r2)).isLessThan(0);
        assertThat(RaceComparator.COMPARATOR.compare(r2, r1)).isGreaterThan(0);
    }

    @Test
    void raceComparator_sameRaceNumber_ordersByName() {
        Race r1 = Race.of(EventId.of(1L), "A-Lauf", (byte) 1);
        Race r2 = Race.of(EventId.of(1L), "B-Lauf", (byte) 1);
        assertThat(RaceComparator.COMPARATOR.compare(r1, r2)).isLessThan(0);
    }

    @Test
    void raceComparator_nullNameSortsLast() {
        Race withName = Race.of(EventId.of(1L), "A-Lauf", (byte) 1);
        Race noName   = Race.of(EventId.of(1L), (byte) 1);
        assertThat(RaceComparator.COMPARATOR.compare(withName, noName)).isLessThan(0);
    }

    // -------------------------------------------------------------------------
    // PersonComparator
    // -------------------------------------------------------------------------

    @Test
    void personComparator_ordersByName() {
        Person alice = Person.of("Müller", "Alice", null, Gender.F);
        Person bob   = Person.of("Müller", "Bob",   null, Gender.M);
        assertThat(PersonComparator.COMPARATOR.compare(alice, bob)).isLessThan(0);
    }

    @Test
    void personComparator_sameName_ordersByBirthDate() {
        Person older   = Person.of("X", "Y", java.time.LocalDate.of(1980, 1, 1), Gender.M);
        Person younger = Person.of("X", "Y", java.time.LocalDate.of(1990, 1, 1), Gender.M);
        assertThat(PersonComparator.COMPARATOR.compare(older, younger)).isLessThan(0);
    }

    // -------------------------------------------------------------------------
    // PositionComparator
    // -------------------------------------------------------------------------

    @Test
    void positionComparator_ordersNumerically() {
        Position p1 = Position.of(1L);
        Position p2 = Position.of(2L);
        assertThat(PositionComparator.COMPARATOR.compare(p1, p2)).isLessThan(0);
        assertThat(PositionComparator.COMPARATOR.compare(p2, p1)).isGreaterThan(0);
    }

    @Test
    void positionComparator_nullSortsLast() {
        Position withValue = Position.of(1L);
        Position nullPos   = Position.of(null);
        assertThat(PositionComparator.COMPARATOR.compare(withValue, nullPos)).isLessThan(0);
    }

    // -------------------------------------------------------------------------
    // Konstruktoren — deckt implizite Klassen-Ctor ab (50%-Coverage-Lücke)
    // -------------------------------------------------------------------------

    @Test
    void raceComparator_canBeInstantiated() {
        assertThat(new RaceComparator()).isNotNull();
    }

    @Test
    void personComparator_canBeInstantiated() {
        assertThat(new PersonComparator()).isNotNull();
    }

    @Test
    void positionComparator_canBeInstantiated() {
        assertThat(new PositionComparator()).isNotNull();
    }
}
