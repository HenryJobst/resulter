package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CupTypeCalculationStrategyTest {

    // Minimal-Implementierung der Interface-Default-Methoden testen
    private static final CupTypeCalculationStrategy STUB = new CupTypeCalculationStrategy() {
        @Override public boolean valid(ClassResult classResult) { return true; }
        @Override public boolean valid(PersonResult personResult) { return true; }
        @Override public boolean valid(Organisation organisation) { return true; }
        @Override public List<CupScore> calculate(Cup cup, List<PersonRaceResult> r, Map<PersonId, OrganisationId> m) { return List.of(); }
    };

    @Test
    void getBestOfRacesCount_default_returnsRacesCount() {
        assertThat(STUB.getBestOfRacesCount(5)).isEqualTo(5);
        assertThat(STUB.getBestOfRacesCount(1)).isEqualTo(1);
    }

    @Test
    void harmonizeClassResultShortName_default_returnsUnchanged() {
        ClassResultShortName cls = ClassResultShortName.of("H21");
        assertThat(STUB.harmonizeClassResultShortName(cls)).isEqualTo(cls);
    }
}
