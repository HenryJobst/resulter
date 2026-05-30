package de.jobst.resulter.domain.scoring;

import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Year;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KJCalculationStrategyTest {

    // -------------------------------------------------------------------------
    // calculate — Positionsbasierte Punktelogik
    // -------------------------------------------------------------------------

    @Test
    void calculate_shouldReturnEmptyListForNoResults() {
        KJCalculationStrategy strategy = new KJCalculationStrategy(null);
        assertThat(strategy.calculate(cup(), List.of(), Map.of())).isEmpty();
    }

    @Test
    void calculate_shouldAssignDescendingPointsWithBonusForTopThree() {
        // n=3 Läufer: defaultPoints=3, bonus startet bei 3
        // Platz 1: 3+3=6, Platz 2: 2+2=4, Platz 3: 1+1=2
        KJCalculationStrategy strategy = new KJCalculationStrategy(null);
        Map<PersonId, OrganisationId> orgs = Map.of(
                PersonId.of(1L), OrganisationId.of(1L),
                PersonId.of(2L), OrganisationId.of(2L),
                PersonId.of(3L), OrganisationId.of(3L));

        List<CupScore> scores = strategy.calculate(cup(), List.of(
                raceResult(1L, "H14", 1000.0, 1L, ResultStatus.OK),
                raceResult(2L, "H14", 1100.0, 2L, ResultStatus.OK),
                raceResult(3L, "H14", 1200.0, 3L, ResultStatus.OK)), orgs);

        assertThat(scores).hasSize(3);
        assertThat(scoreOf(scores, 1L)).isEqualTo(6.0);
        assertThat(scoreOf(scores, 2L)).isEqualTo(4.0);
        assertThat(scoreOf(scores, 3L)).isEqualTo(2.0);
    }

    @Test
    void calculate_shouldAssignCorrectPointsForFourRunners() {
        // n=4: defaultPoints=4, bonus=3
        // Platz 1: 4+3=7, Platz 2: 3+2=5, Platz 3: 2+1=3, Platz 4: 1+0=1
        KJCalculationStrategy strategy = new KJCalculationStrategy(null);
        Map<PersonId, OrganisationId> orgs = Map.of(
                PersonId.of(1L), OrganisationId.of(1L),
                PersonId.of(2L), OrganisationId.of(2L),
                PersonId.of(3L), OrganisationId.of(3L),
                PersonId.of(4L), OrganisationId.of(4L));

        List<CupScore> scores = strategy.calculate(cup(), List.of(
                raceResult(1L, "H14", 1000.0, 1L, ResultStatus.OK),
                raceResult(2L, "H14", 1100.0, 2L, ResultStatus.OK),
                raceResult(3L, "H14", 1200.0, 3L, ResultStatus.OK),
                raceResult(4L, "H14", 1300.0, 4L, ResultStatus.OK)), orgs);

        assertThat(scores).hasSize(4);
        assertThat(scoreOf(scores, 1L)).isEqualTo(7.0);
        assertThat(scoreOf(scores, 2L)).isEqualTo(5.0);
        assertThat(scoreOf(scores, 3L)).isEqualTo(3.0);
        assertThat(scoreOf(scores, 4L)).isEqualTo(1.0);
    }

    @Test
    void calculate_shouldAssignSamePointsToTiedPositions() {
        // n=3: Platz 1 doppelt belegt → beide 6 Punkte; Platz 3: 1+1=2
        KJCalculationStrategy strategy = new KJCalculationStrategy(null);
        Map<PersonId, OrganisationId> orgs = Map.of(
                PersonId.of(1L), OrganisationId.of(1L),
                PersonId.of(2L), OrganisationId.of(2L),
                PersonId.of(3L), OrganisationId.of(3L));

        List<CupScore> scores = strategy.calculate(cup(), List.of(
                raceResult(1L, "H14", 1000.0, 1L, ResultStatus.OK),
                raceResult(2L, "H14", 1000.0, 1L, ResultStatus.OK), // gleiche Position
                raceResult(3L, "H14", 1200.0, 3L, ResultStatus.OK)), orgs);

        assertThat(scores).hasSize(3);
        assertThat(scoreOf(scores, 1L)).isEqualTo(6.0);
        assertThat(scoreOf(scores, 2L)).isEqualTo(6.0);
        assertThat(scoreOf(scores, 3L)).isEqualTo(2.0);
    }

    @Test
    void calculate_shouldAssign1PointToDisqualifiedRunners() {
        // MISSING_PUNCH, DID_NOT_FINISH, DISQUALIFIED → jeweils 1 Punkt
        KJCalculationStrategy strategy = new KJCalculationStrategy(null);
        Map<PersonId, OrganisationId> orgs = Map.of(
                PersonId.of(1L), OrganisationId.of(1L),
                PersonId.of(2L), OrganisationId.of(2L),
                PersonId.of(3L), OrganisationId.of(3L));

        List<CupScore> scores = strategy.calculate(cup(), List.of(
                raceResult(1L, "H14", null, null, ResultStatus.MISSING_PUNCH),
                raceResult(2L, "H14", null, null, ResultStatus.DID_NOT_FINISH),
                raceResult(3L, "H14", null, null, ResultStatus.DISQUALIFIED)), orgs);

        assertThat(scores).hasSize(3);
        scores.forEach(s -> assertThat(s.score()).isEqualTo(1.0));
    }

    @Test
    void calculate_shouldMixOkAndDisqualifiedRunners() {
        // n=3 gesamt: 2 OK + 1 DNF → defaultPoints=3
        // Platz 1 (OK): 3+3=6, Platz 2 (OK): 2+2=4, DNF: 1
        KJCalculationStrategy strategy = new KJCalculationStrategy(null);
        Map<PersonId, OrganisationId> orgs = Map.of(
                PersonId.of(1L), OrganisationId.of(1L),
                PersonId.of(2L), OrganisationId.of(2L),
                PersonId.of(3L), OrganisationId.of(3L));

        List<CupScore> scores = strategy.calculate(cup(), List.of(
                raceResult(1L, "H14", 1000.0, 1L, ResultStatus.OK),
                raceResult(2L, "H14", 1100.0, 2L, ResultStatus.OK),
                raceResult(3L, "H14", null, null, ResultStatus.DID_NOT_FINISH)), orgs);

        assertThat(scores).hasSize(3);
        assertThat(scoreOf(scores, 1L)).isEqualTo(6.0);
        assertThat(scoreOf(scores, 2L)).isEqualTo(4.0);
        assertThat(scoreOf(scores, 3L)).isEqualTo(1.0);
    }

    // -------------------------------------------------------------------------
    // valid(ClassResult) — nur Jugendklassen
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"D10", "D12", "D14", "D16", "D18", "H10", "H12", "H14", "H16", "H18"})
    void valid_classResult_shouldAcceptYouthClasses(String youthClass) {
        assertThat(new KJCalculationStrategy(null).valid(classResult(youthClass))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"H21", "D21", "H20", "HAK", "BK", "H10B"})
    void valid_classResult_shouldRejectNonYouthClasses(String nonYouth) {
        assertThat(new KJCalculationStrategy(null).valid(classResult(nonYouth))).isFalse();
    }

    // -------------------------------------------------------------------------
    // valid(Organisation)
    // -------------------------------------------------------------------------

    @Test
    void valid_organisation_shouldReturnTrueWhenNoValidClubsConfigured() {
        // Ohne Map: validClubs ist leer → kann nicht prüfen → true
        KJCalculationStrategy strategy = new KJCalculationStrategy(null);
        assertThat(strategy.valid(Organisation.of(99L, "Beliebiger Verein", "BV"))).isTrue();
    }

    @Test
    void valid_organisation_shouldReturnFalseForUnknownClubWhenValidClubsConfigured() {
        KJCalculationStrategy strategy = new KJCalculationStrategy(kjOrganisationMap());
        // Org-ID 99 ist kein gültiger Verein im KJ-Verband
        assertThat(strategy.valid(Organisation.of(99L, "Fremder Verein", "FV"))).isFalse();
    }

    @Test
    void valid_organisation_shouldReturnTrueForValidClub() {
        KJCalculationStrategy strategy = new KJCalculationStrategy(kjOrganisationMap());
        // Club-ID 20 und 21 wurden über KJ→Region→Club aufgelöst
        assertThat(strategy.valid(Organisation.of(20L, "KJ-Verein A", "KJA"))).isTrue();
        assertThat(strategy.valid(Organisation.of(21L, "KJ-Verein B", "KJB"))).isTrue();
    }

    // -------------------------------------------------------------------------
    // valid(PersonResult)
    // -------------------------------------------------------------------------

    @Test
    void valid_personResult_shouldReturnFalseWhenNoValidClubsAndPersonHasOrg() {
        KJCalculationStrategy strategy = new KJCalculationStrategy(null);
        // validClubs ist leer → validClubs.contains() → false
        PersonResult personResult = PersonResult.of(
                ClassResultShortName.of("H14"), PersonId.of(1L), OrganisationId.of(5L), List.of());
        assertThat(strategy.valid(personResult)).isFalse();
    }

    @Test
    void valid_personResult_shouldReturnTrueForPersonInValidClub() {
        KJCalculationStrategy strategy = new KJCalculationStrategy(kjOrganisationMap());
        PersonResult personResult = PersonResult.of(
                ClassResultShortName.of("H14"), PersonId.of(1L), OrganisationId.of(20L), List.of());
        assertThat(strategy.valid(personResult)).isTrue();
    }

    @Test
    void valid_personResult_shouldReturnFalseForPersonNotInValidClub() {
        KJCalculationStrategy strategy = new KJCalculationStrategy(kjOrganisationMap());
        PersonResult personResult = PersonResult.of(
                ClassResultShortName.of("H14"), PersonId.of(1L), OrganisationId.of(99L), List.of());
        assertThat(strategy.valid(personResult)).isFalse();
    }

    // -------------------------------------------------------------------------
    // harmonizeClassResultShortName
    // -------------------------------------------------------------------------

    @Test
    void harmonizeClassResultShortName_shouldStripWhitespaceAndRemoveDash() {
        KJCalculationStrategy strategy = new KJCalculationStrategy(null);
        assertThat(strategy.harmonizeClassResultShortName(ClassResultShortName.of(" H-14 ")).value()).isEqualTo("H14");
        assertThat(strategy.harmonizeClassResultShortName(ClassResultShortName.of("D16")).value()).isEqualTo("D16");
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    private static Cup cup() {
        return Cup.of(1L, "KJ Cup", CupType.KJ, Year.of(2025), List.of());
    }

    private static PersonRaceResult raceResult(
            Long personId, String className, Double runtime, Long position, ResultStatus status) {
        return PersonRaceResult.of(className, personId, null, null, runtime, position, (byte) 1, status);
    }

    private static ClassResult classResult(String shortName) {
        return ClassResult.of(shortName, shortName, null, null, null);
    }

    private static double scoreOf(List<CupScore> scores, long personId) {
        return scores.stream()
                .filter(s -> s.personId().value().equals(personId))
                .findFirst()
                .orElseThrow()
                .score();
    }

    /**
     * Baut eine minimale KJ-Organisationshierarchie:
     *   KJ (id=1) → Region (id=10) → Club A (id=20), Club B (id=21)
     * Resultat: validClubs = {20, 21}
     */
    private static Map<OrganisationId, Organisation> kjOrganisationMap() {
        OrganisationId kjId = OrganisationId.of(1L);
        OrganisationId regionId = OrganisationId.of(10L);
        OrganisationId clubAId = OrganisationId.of(20L);
        OrganisationId clubBId = OrganisationId.of(21L);

        Organisation kjOrg = Organisation.of(
                1L, "Kinder-Jugend", "KJ", "Other", null, List.of(regionId));
        Organisation region = Organisation.of(
                10L, "Region", "REG", "Other", null, List.of(clubAId, clubBId));

        return Map.of(kjId, kjOrg, regionId, region);
    }
}
