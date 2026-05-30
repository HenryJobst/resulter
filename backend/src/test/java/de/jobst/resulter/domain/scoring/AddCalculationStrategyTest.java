package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AddCalculationStrategyTest {

    private final AddCalculationStrategy strategy = new AddCalculationStrategy();
    private final Cup cup = Cup.of(1L, "Add Cup", CupType.ADD, Year.of(2025), List.of());

    @Test
    void valid_classResult_shouldAlwaysReturnFalse() {
        assertThat(strategy.valid(ClassResult.of("H21", "H21", null, null, null))).isFalse();
        assertThat(strategy.valid(ClassResult.of("D10", "D10", null, null, null))).isFalse();
    }

    @Test
    void valid_personResult_shouldAlwaysReturnFalse() {
        PersonResult personResult = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(1L), OrganisationId.of(1L), List.of());
        assertThat(strategy.valid(personResult)).isFalse();
    }

    @Test
    void valid_organisation_shouldAlwaysReturnFalse() {
        assertThat(strategy.valid(Organisation.of("Beliebiger Verein", "BV"))).isFalse();
    }

    @Test
    void calculate_shouldAlwaysReturnEmptyList() {
        PersonRaceResult result = PersonRaceResult.of("H21", 1L, null, null, 1000.0, 1L, (byte) 1, ResultStatus.OK);

        assertThat(strategy.calculate(cup, List.of(), Map.of())).isEmpty();
        assertThat(strategy.calculate(cup, List.of(result), Map.of(PersonId.of(1L), OrganisationId.of(1L)))).isEmpty();
    }
}
