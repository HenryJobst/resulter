package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Year;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class NebelCalculationStrategyTest {

    // -------------------------------------------------------------------------
    // calculate — Punktelogik (NOR-Formel mit Pro-Org-Deduplication)
    // -------------------------------------------------------------------------

    @Test
    void calculate_shouldReturnEmptyListForNoResults() {
        NebelCalculationStrategy strategy = new NebelCalculationStrategy(Map.of());
        assertThat(strategy.calculate(cup(), List.of(), Map.of())).isEmpty();
    }

    @Test
    void calculate_shouldAssign12PointsToWinner() {
        NebelCalculationStrategy strategy = new NebelCalculationStrategy(Map.of());

        List<CupScore> scores = strategy.calculate(
                cup(),
                List.of(raceResult(1L, "H21", 1000.0)),
                Map.of(PersonId.of(1L), OrganisationId.of(1L)));

        assertThat(scores).hasSize(1);
        assertThat(scores.getFirst().score()).isEqualTo(12.0);
    }

    @Test
    void calculate_shouldApplyNorPointsRelativeToBestTime() {
        NebelCalculationStrategy strategy = new NebelCalculationStrategy(Map.of());

        // Sieger: 1000s → 12P; Zweiter: 1050s = exakt 1.05× → 11P; Dritter: 1500s = 1.50× → 5P
        List<CupScore> scores = strategy.calculate(
                cup(),
                List.of(
                        raceResult(1L, "H21", 1000.0),
                        raceResult(2L, "H21", 1050.0),
                        raceResult(3L, "H21", 1500.0)),
                Map.of(
                        PersonId.of(1L), OrganisationId.of(1L),
                        PersonId.of(2L), OrganisationId.of(2L),
                        PersonId.of(3L), OrganisationId.of(3L)));

        assertThat(scores).hasSize(3);
        assertThat(scores.get(0).score()).isEqualTo(12.0);
        assertThat(scores.get(1).score()).isEqualTo(11.0);
        assertThat(scores.get(2).score()).isEqualTo(5.0);
    }

    @Test
    void calculate_shouldScoreOnlyFirstRunnerPerOrganisation() {
        NebelCalculationStrategy strategy = new NebelCalculationStrategy(Map.of());

        OrganisationId sameOrg = OrganisationId.of(1L);

        // Runner2 gehört zur selben Org wie Runner1 → wird ausgeschlossen
        List<CupScore> scores = strategy.calculate(
                cup(),
                List.of(
                        raceResult(1L, "H21", 1000.0),
                        raceResult(2L, "H21", 1100.0),
                        raceResult(3L, "H21", 1200.0)),
                Map.of(
                        PersonId.of(1L), sameOrg,
                        PersonId.of(2L), sameOrg,
                        PersonId.of(3L), OrganisationId.of(2L)));

        assertThat(scores).hasSize(2);
        assertThat(scores.stream().map(s -> s.personId().value()).toList())
                .containsExactlyInAnyOrder(1L, 3L);
    }

    @Test
    void calculate_shouldExcludeRunnerWithoutOrganisationMapping() {
        NebelCalculationStrategy strategy = new NebelCalculationStrategy(Map.of());

        List<CupScore> scores = strategy.calculate(
                cup(),
                List.of(
                        raceResult(1L, "H21", 1000.0),
                        raceResult(2L, "H21", 1100.0)),
                Map.of(PersonId.of(1L), OrganisationId.of(1L)));
        // Runner2 fehlt in der Map

        assertThat(scores).hasSize(1);
        assertThat(scores.getFirst().personId().value()).isEqualTo(1L);
    }

    // -------------------------------------------------------------------------
    // valid(ClassResult)
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"BK", "BL", "Beg", "Trim", "Trimm", "Beginner", "OffK", "OffL", "D/H-12 Be",
        "D/H -12b", "Begleitung"})
    void valid_classResult_shouldRejectSkippedClasses(String skipped) {
        assertThat(new NebelCalculationStrategy(Map.of()).valid(classResult(skipped))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"H21", "D21", "H19L", "D19K"})
    void valid_classResult_shouldAcceptRegularClasses(String regular) {
        assertThat(new NebelCalculationStrategy(Map.of()).valid(classResult(regular))).isTrue();
    }

    // -------------------------------------------------------------------------
    // valid(Organisation)
    // -------------------------------------------------------------------------

    @Test
    void valid_organisation_shouldRejectOrganisationsContainingSkippedName() {
        NebelCalculationStrategy strategy = new NebelCalculationStrategy(Map.of());

        assertThat(strategy.valid(Organisation.of("ohne Verein", "ohne"))).isFalse();
        assertThat(strategy.valid(Organisation.of("Volkssport", "Volkssport"))).isFalse();
        // ShortName "Volkssport Berlin" enthält "Volkssport" → ebenfalls abgelehnt
        assertThat(strategy.valid(Organisation.of("Volkssport Berlin e.V.", "Volkssport Berlin"))).isFalse();
    }

    @Test
    void valid_organisation_shouldAcceptRegularOrganisations() {
        NebelCalculationStrategy strategy = new NebelCalculationStrategy(Map.of());

        assertThat(strategy.valid(Organisation.of("Kaulsdorfer OLV", "KOLV"))).isTrue();
        assertThat(strategy.valid(Organisation.of("OLV Berlin", "OLVB"))).isTrue();
    }

    // -------------------------------------------------------------------------
    // valid(PersonResult)
    // -------------------------------------------------------------------------

    @Test
    void valid_personResult_shouldThrowWhenOrganisationMapIsNull() {
        NebelCalculationStrategy strategy = new NebelCalculationStrategy(null);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> strategy.valid(personResult(1L, OrganisationId.of(1L))));
    }

    @Test
    void valid_personResult_shouldRejectRunnerFromSkippedOrganisation() {
        OrganisationId orgId = OrganisationId.of(1L);
        NebelCalculationStrategy strategy = new NebelCalculationStrategy(
                Map.of(orgId, Organisation.of(1L, "ohne Verein", "ohne")));

        assertThat(strategy.valid(personResult(1L, orgId))).isFalse();
    }

    @Test
    void valid_personResult_shouldAcceptRunnerFromRegularOrganisation() {
        OrganisationId orgId = OrganisationId.of(2L);
        NebelCalculationStrategy strategy = new NebelCalculationStrategy(
                Map.of(orgId, Organisation.of(2L, "Kaulsdorfer OLV", "KOLV")));

        assertThat(strategy.valid(personResult(1L, orgId))).isTrue();
    }

    @Test
    void valid_personResult_shouldReturnFalseWhenOrganisationNotInMap() {
        NebelCalculationStrategy strategy = new NebelCalculationStrategy(Map.of());
        assertThat(strategy.valid(personResult(1L, OrganisationId.of(99L)))).isFalse();
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    private static Cup cup() {
        return Cup.of(1L, "Nebel Cup", CupType.NEBEL, Year.of(2025), List.of());
    }

    private static PersonRaceResult raceResult(Long personId, String className, Double runtime) {
        return PersonRaceResult.of(className, personId, null, null, runtime, 1L, (byte) 1, ResultStatus.OK);
    }

    private static ClassResult classResult(String shortName) {
        return ClassResult.of(shortName, shortName, null, null, null);
    }

    private static PersonResult personResult(Long personId, OrganisationId orgId) {
        return PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(personId), orgId, List.of());
    }
}
