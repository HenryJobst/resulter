package de.jobst.resulter.domain;

import de.jobst.resulter.domain.scoring.*;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CupTest {

    // -------------------------------------------------------------------------
    // compareTo — alphabetisch nach Name
    // -------------------------------------------------------------------------

    @Test
    void compareTo_ordersByNameAlphabetically() {
        Cup a = Cup.of(1L, "Alpha Cup", CupType.NOR, Year.of(2025), List.of());
        Cup b = Cup.of(2L, "Beta Cup", CupType.NOR, Year.of(2025), List.of());

        assertThat(a.compareTo(b)).isLessThan(0);
        assertThat(b.compareTo(a)).isGreaterThan(0);
        assertThat(a.compareTo(a)).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // getCupTypeCalculationStrategy — liefert korrekte Strategie je CupType
    // -------------------------------------------------------------------------

    @Test
    void getCupTypeCalculationStrategy_norType_returnsNORStrategy() {
        Cup cup = Cup.of(1L, "Cup", CupType.NOR, Year.of(2025), List.of());
        assertThat(cup.getCupTypeCalculationStrategy(null)).isInstanceOf(NORCalculationStrategy.class);
    }

    @Test
    void getCupTypeCalculationStrategy_kjType_returnsKJStrategy() {
        Cup cup = Cup.of(1L, "Cup", CupType.KJ, Year.of(2025), List.of());
        assertThat(cup.getCupTypeCalculationStrategy(null)).isInstanceOf(KJCalculationStrategy.class);
    }

    @Test
    void getCupTypeCalculationStrategy_kristallType_returnsKristallStrategy() {
        Cup cup = Cup.of(1L, "Cup", CupType.KRISTALL, Year.of(2025), List.of());
        assertThat(cup.getCupTypeCalculationStrategy(null)).isInstanceOf(KristallCalculationStrategy.class);
    }

    @Test
    void getCupTypeCalculationStrategy_nebelType_returnsNebelStrategy() {
        Cup cup = Cup.of(1L, "Cup", CupType.NEBEL, Year.of(2025), List.of());
        assertThat(cup.getCupTypeCalculationStrategy(null)).isInstanceOf(NebelCalculationStrategy.class);
    }

    @Test
    void getCupTypeCalculationStrategy_addType_returnsAddStrategy() {
        Cup cup = Cup.of(1L, "Cup", CupType.ADD, Year.of(2025), List.of());
        assertThat(cup.getCupTypeCalculationStrategy(null)).isInstanceOf(AddCalculationStrategy.class);
    }

    // -------------------------------------------------------------------------
    // of() — grundlegende Felder
    // -------------------------------------------------------------------------

    @Test
    void of_setsAllFields() {
        Cup cup = Cup.of(42L, "NOR Cup", CupType.NOR, Year.of(2024), List.of(EventId.of(1L)));

        assertThat(cup.getId().value()).isEqualTo(42L);
        assertThat(cup.getName().value()).isEqualTo("NOR Cup");
        assertThat(cup.getType()).isEqualTo(CupType.NOR);
        assertThat(cup.getYear()).isEqualTo(Year.of(2024));
        assertThat(cup.getEventIds()).containsExactly(EventId.of(1L));
    }

    @Test
    void of_nullId_usesEmptyId() {
        Cup cup = Cup.of(null, "Test", CupType.NOR, Year.of(2025), List.of());
        assertThat(cup.getId().isPersistent()).isFalse();
    }

    // -------------------------------------------------------------------------
    // update()
    // -------------------------------------------------------------------------

    @Test
    void update_changesAllMutableFields() {
        Cup cup = Cup.of(1L, "Old Name", CupType.NOR, Year.of(2024), List.of());

        Cup result = cup.update(CupName.of("New Name"), CupType.KJ, Year.of(2025), List.of(EventId.of(99L)));

        assertThat(result).isSameAs(cup);
        assertThat(cup.getName().value()).isEqualTo("New Name");
        assertThat(cup.getType()).isEqualTo(CupType.KJ);
        assertThat(cup.getYear()).isEqualTo(Year.of(2025));
        assertThat(cup.getEventIds()).containsExactly(EventId.of(99L));
    }
}
