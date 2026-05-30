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

class KristallCalculationStrategyTest {

    // -------------------------------------------------------------------------
    // calculate — Punktelogik
    // -------------------------------------------------------------------------

    @Test
    void calculate_shouldReturnEmptyListForNoResults() {
        KristallCalculationStrategy strategy = new KristallCalculationStrategy(Map.of());
        Cup cup = cup();

        assertThat(strategy.calculate(cup, List.of(), Map.of())).isEmpty();
    }

    @Test
    void calculate_shouldAssign10PointsToSingleWinner() {
        KristallCalculationStrategy strategy = new KristallCalculationStrategy(Map.of());
        Cup cup = cup();

        OrganisationId orgA = OrganisationId.of(1L);
        PersonRaceResult winner = raceResult(1L, "H21", 1000.0);

        List<CupScore> scores = strategy.calculate(
                cup,
                List.of(winner),
                Map.of(PersonId.of(1L), orgA));

        assertThat(scores).hasSize(1);
        assertThat(scores.getFirst().score()).isEqualTo(10.0);
    }

    @Test
    void calculate_shouldAssignDescendingPointsToRunnersWithDifferentTimes() {
        KristallCalculationStrategy strategy = new KristallCalculationStrategy(Map.of());
        Cup cup = cup();

        // Drei Läufer, verschiedene Zeiten → 10, 9, 8
        List<CupScore> scores = strategy.calculate(
                cup,
                List.of(
                        raceResult(1L, "H21", 1000.0),
                        raceResult(2L, "H21", 1100.0),
                        raceResult(3L, "H21", 1200.0)
                ),
                Map.of(
                        PersonId.of(1L), OrganisationId.of(1L),
                        PersonId.of(2L), OrganisationId.of(2L),
                        PersonId.of(3L), OrganisationId.of(3L)
                ));

        assertThat(scores).hasSize(3);
        assertThat(scores.get(0).score()).isEqualTo(10.0);
        assertThat(scores.get(1).score()).isEqualTo(9.0);
        assertThat(scores.get(2).score()).isEqualTo(8.0);
    }

    @Test
    void calculate_shouldAssignSamePointsToTiedRunnersAndDeductGroupSizeForNext() {
        KristallCalculationStrategy strategy = new KristallCalculationStrategy(Map.of());
        Cup cup = cup();

        // Runner1 und Runner2 gleiche Zeit (1000s) → beide 10 Punkte
        // Runner3 andere Zeit → 10 - 2 (Gruppengröße) = 8 Punkte
        List<CupScore> scores = strategy.calculate(
                cup,
                List.of(
                        raceResult(1L, "H21", 1000.0),
                        raceResult(2L, "H21", 1000.0),
                        raceResult(3L, "H21", 1200.0)
                ),
                Map.of(
                        PersonId.of(1L), OrganisationId.of(1L),
                        PersonId.of(2L), OrganisationId.of(2L),
                        PersonId.of(3L), OrganisationId.of(3L)
                ));

        assertThat(scores).hasSize(3);
        assertThat(scores.get(0).score()).isEqualTo(10.0);
        assertThat(scores.get(1).score()).isEqualTo(10.0);
        assertThat(scores.get(2).score()).isEqualTo(8.0);
    }

    @Test
    void calculate_shouldNeverAssignLessThan1Point() {
        KristallCalculationStrategy strategy = new KristallCalculationStrategy(Map.of());
        Cup cup = cup();

        // 15 Läufer → Punkte fallen schnell unter 1, aber Minimum ist 1
        List<PersonRaceResult> results = new java.util.ArrayList<>();
        Map<PersonId, OrganisationId> orgMap = new java.util.HashMap<>();
        for (long i = 1; i <= 15; i++) {
            results.add(raceResult(i, "H21", 1000.0 + i * 100.0));
            orgMap.put(PersonId.of(i), OrganisationId.of(i));
        }

        List<CupScore> scores = strategy.calculate(cup, results, orgMap);

        assertThat(scores).hasSize(15);
        scores.forEach(score ->
                assertThat(score.score()).isGreaterThanOrEqualTo(1.0)
        );
    }

    @Test
    void calculate_shouldScoreOnlyFirstRunnerPerOrganisation() {
        KristallCalculationStrategy strategy = new KristallCalculationStrategy(Map.of());
        Cup cup = cup();

        OrganisationId sameOrg = OrganisationId.of(1L);
        OrganisationId otherOrg = OrganisationId.of(2L);

        // Runner1 und Runner2 gehören zur selben Organisation → nur Runner1 (schneller) zählt
        List<CupScore> scores = strategy.calculate(
                cup,
                List.of(
                        raceResult(1L, "H21", 1000.0),
                        raceResult(2L, "H21", 1100.0), // selbe Org → wird ausgeschlossen
                        raceResult(3L, "H21", 1200.0)
                ),
                Map.of(
                        PersonId.of(1L), sameOrg,
                        PersonId.of(2L), sameOrg,
                        PersonId.of(3L), otherOrg
                ));

        assertThat(scores).hasSize(2);
        assertThat(scores.stream().map(s -> s.personId().value()).toList())
                .containsExactlyInAnyOrder(1L, 3L);
        // Runner1: 10 Punkte (Platz 1), Runner3: 9 Punkte (Platz 2 nach Deduplication)
        assertThat(scores.stream().filter(s -> s.personId().value().equals(1L))
                .findFirst().orElseThrow().score()).isEqualTo(10.0);
        assertThat(scores.stream().filter(s -> s.personId().value().equals(3L))
                .findFirst().orElseThrow().score()).isEqualTo(9.0);
    }

    @Test
    void calculate_shouldExcludeRunnerWithoutOrganisationMapping() {
        KristallCalculationStrategy strategy = new KristallCalculationStrategy(Map.of());
        Cup cup = cup();

        // Runner2 hat keinen Eintrag in organisationByPerson → wird übersprungen
        List<CupScore> scores = strategy.calculate(
                cup,
                List.of(
                        raceResult(1L, "H21", 1000.0),
                        raceResult(2L, "H21", 1100.0),
                        raceResult(3L, "H21", 1200.0)
                ),
                Map.of(
                        PersonId.of(1L), OrganisationId.of(1L),
                        // PersonId.of(2L) fehlt absichtlich
                        PersonId.of(3L), OrganisationId.of(3L)
                ));

        assertThat(scores).hasSize(2);
        assertThat(scores.stream().map(s -> s.personId().value()).toList())
                .containsExactlyInAnyOrder(1L, 3L);
    }

    @Test
    void calculate_shouldCarryPersonAndClassInfoToScore() {
        KristallCalculationStrategy strategy = new KristallCalculationStrategy(Map.of());
        Cup cup = cup();

        OrganisationId orgId = OrganisationId.of(42L);
        List<CupScore> scores = strategy.calculate(
                cup,
                List.of(raceResult(7L, "D21", 900.0)),
                Map.of(PersonId.of(7L), orgId));

        CupScore score = scores.getFirst();
        assertThat(score.personId().value()).isEqualTo(7L);
        assertThat(score.organisationId()).isEqualTo(orgId);
        assertThat(score.classResultShortName().value()).isEqualTo("D21");
    }

    // -------------------------------------------------------------------------
    // valid(ClassResult) — Klassen-Sperrfilter
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"BK", "BL", "Beg", "Trim", "Beginner", "OffK", "OffL", "D/H-12 Be"})
    void valid_classResult_shouldRejectSkippedClasses(String skipped) {
        KristallCalculationStrategy strategy = new KristallCalculationStrategy(Map.of());
        assertThat(strategy.valid(classResult(skipped))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"H21", "D21", "H19L", "D19K", "HAK"})
    void valid_classResult_shouldAcceptRegularClasses(String regular) {
        KristallCalculationStrategy strategy = new KristallCalculationStrategy(Map.of());
        assertThat(strategy.valid(classResult(regular))).isTrue();
    }

    // -------------------------------------------------------------------------
    // valid(Organisation) — Organisations-Sperrfilter
    // -------------------------------------------------------------------------

    @Test
    void valid_organisation_shouldRejectSkippedOrganisationNames() {
        KristallCalculationStrategy strategy = new KristallCalculationStrategy(Map.of());

        assertThat(strategy.valid(Organisation.of("ohne Verein", "ohne"))).isFalse();
        assertThat(strategy.valid(Organisation.of("Volkssport Berlin", "Volkssport Berlin"))).isFalse();
        assertThat(strategy.valid(Organisation.of("Volkssport", "Volkssport"))).isFalse();
    }

    @Test
    void valid_organisation_shouldAcceptRegularOrganisations() {
        KristallCalculationStrategy strategy = new KristallCalculationStrategy(Map.of());

        assertThat(strategy.valid(Organisation.of("Kaulsdorfer OLV", "KOLV"))).isTrue();
        assertThat(strategy.valid(Organisation.of("OLV Berlin", "OLVB"))).isTrue();
    }

    // -------------------------------------------------------------------------
    // valid(PersonResult) — Personen-Sperrfilter
    // -------------------------------------------------------------------------

    @Test
    void valid_personResult_shouldThrowWhenOrganisationMapIsNull() {
        KristallCalculationStrategy strategy = new KristallCalculationStrategy(null);

        PersonResult personResult = PersonResult.of(
                ClassResultShortName.of("H21"),
                PersonId.of(1L),
                OrganisationId.of(1L),
                List.of());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> strategy.valid(personResult));
    }

    @Test
    void valid_personResult_shouldRejectRunnerFromSkippedOrganisation() {
        OrganisationId orgId = OrganisationId.of(1L);
        Organisation skippedOrg = Organisation.of(1L, "ohne Verein", "ohne");

        KristallCalculationStrategy strategy = new KristallCalculationStrategy(Map.of(orgId, skippedOrg));

        PersonResult personResult = PersonResult.of(
                ClassResultShortName.of("H21"),
                PersonId.of(1L),
                orgId,
                List.of());

        assertThat(strategy.valid(personResult)).isFalse();
    }

    @Test
    void valid_personResult_shouldAcceptRunnerFromRegularOrganisation() {
        OrganisationId orgId = OrganisationId.of(2L);
        Organisation regularOrg = Organisation.of(2L, "Kaulsdorfer OLV", "KOLV");

        KristallCalculationStrategy strategy = new KristallCalculationStrategy(Map.of(orgId, regularOrg));

        PersonResult personResult = PersonResult.of(
                ClassResultShortName.of("H21"),
                PersonId.of(1L),
                orgId,
                List.of());

        assertThat(strategy.valid(personResult)).isTrue();
    }

    @Test
    void valid_personResult_shouldReturnFalseWhenOrganisationNotInMap() {
        KristallCalculationStrategy strategy = new KristallCalculationStrategy(Map.of());

        PersonResult personResult = PersonResult.of(
                ClassResultShortName.of("H21"),
                PersonId.of(1L),
                OrganisationId.of(99L), // nicht in der Map
                List.of());

        assertThat(strategy.valid(personResult)).isFalse();
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    private static Cup cup() {
        return Cup.of(1L, "Kristall Cup", CupType.KRISTALL, Year.of(2025), List.of());
    }

    private static PersonRaceResult raceResult(Long personId, String className, Double runtime) {
        return PersonRaceResult.of(className, personId, null, null, runtime, 1L, (byte) 1, ResultStatus.OK);
    }

    private static ClassResult classResult(String shortName) {
        return ClassResult.of(shortName, shortName, null, null, null);
    }
}
