package de.jobst.resulter.domain.aggregations;

import de.jobst.resulter.domain.*;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AggregationStatisticsTest {

    private static Organisation org(String name) {
        return Organisation.of(name, name);
    }

    // -------------------------------------------------------------------------
    // OrganisationStatistics.of() — Verhältnisberechnung
    // -------------------------------------------------------------------------

    @Test
    void organisationStatistics_of_calculatesRatiosCorrectly() {
        OrganisationStatistics stats = OrganisationStatistics.of(org("OLV"), 10, 20, 2);

        assertThat(stats.runnerCount()).isEqualTo(10);
        assertThat(stats.totalStarts()).isEqualTo(20);
        assertThat(stats.nonScoringStarts()).isEqualTo(2);
        assertThat(stats.startsPerRunner()).isEqualTo(2.0);
        assertThat(stats.nonScoringStartsPerRunner()).isEqualTo(0.2);
        assertThat(stats.nonScoringRatio()).isEqualTo(0.1);
    }

    @Test
    void organisationStatistics_of_returnsZeroRatiosWhenNoRunners() {
        OrganisationStatistics stats = OrganisationStatistics.of(org("OLV"), 0, 0, 0);

        assertThat(stats.startsPerRunner()).isEqualTo(0.0);
        assertThat(stats.nonScoringStartsPerRunner()).isEqualTo(0.0);
        assertThat(stats.nonScoringRatio()).isEqualTo(0.0);
    }

    @Test
    void organisationStatistics_of_returnsZeroNonScoringRatioWhenNoStarts() {
        OrganisationStatistics stats = OrganisationStatistics.of(org("OLV"), 5, 0, 0);

        assertThat(stats.nonScoringRatio()).isEqualTo(0.0);
    }

    @Test
    void organisationStatistics_compareTo_sortsByRunnerCountDescending() {
        OrganisationStatistics large = OrganisationStatistics.of(org("Groß"), 20, 40, 0);
        OrganisationStatistics small = OrganisationStatistics.of(org("Klein"), 5, 10, 0);

        assertThat(large.compareTo(small)).isLessThan(0);  // 20 > 5, so large comes first
        assertThat(small.compareTo(large)).isGreaterThan(0);
        assertThat(large.compareTo(large)).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // CupOverallStatistics.of() — Kennzahlen
    // -------------------------------------------------------------------------

    @Test
    void cupOverallStatistics_of_calculatesCorrectly() {
        CupOverallStatistics stats = CupOverallStatistics.of(100, 5, 200, 10);

        assertThat(stats.totalRunners()).isEqualTo(100);
        assertThat(stats.totalOrganisations()).isEqualTo(5);
        assertThat(stats.totalStarts()).isEqualTo(200);
        assertThat(stats.totalNonScoringStarts()).isEqualTo(10);
        assertThat(stats.runnersPerOrganisation()).isEqualTo(20.0);
        assertThat(stats.startsPerOrganisation()).isEqualTo(40.0);
        assertThat(stats.nonScoringStartsPerOrganisation()).isEqualTo(2.0);
        assertThat(stats.startsPerRunner()).isEqualTo(2.0);
        assertThat(stats.nonScoringStartsPerRunner()).isEqualTo(0.1);
    }

    @Test
    void cupOverallStatistics_of_returnsZeroRatiosWhenEmpty() {
        CupOverallStatistics stats = CupOverallStatistics.of(0, 0, 0, 0);

        assertThat(stats.runnersPerOrganisation()).isEqualTo(0.0);
        assertThat(stats.startsPerOrganisation()).isEqualTo(0.0);
        assertThat(stats.startsPerRunner()).isEqualTo(0.0);
    }

    // -------------------------------------------------------------------------
    // PersonWithScore.compareTo() — absteigend nach Score
    // -------------------------------------------------------------------------

    @Test
    void personWithScore_compareTo_sortsByScoreDescending() {
        PersonWithScore higher = new PersonWithScore(PersonId.of(1L), 15.0, ClassResultShortName.of("H21"));
        PersonWithScore lower = new PersonWithScore(PersonId.of(2L), 8.0, ClassResultShortName.of("H21"));

        assertThat(higher.compareTo(lower)).isLessThan(0); // descending: 15 > 8, so higher comes first
        assertThat(lower.compareTo(higher)).isGreaterThan(0);
    }

    @Test
    void personWithScore_compareTo_sameScore_ordersByClassName() {
        PersonWithScore d21 = new PersonWithScore(PersonId.of(1L), 10.0, ClassResultShortName.of("D21"));
        PersonWithScore h21 = new PersonWithScore(PersonId.of(1L), 10.0, ClassResultShortName.of("H21"));

        assertThat(d21.compareTo(h21)).isLessThan(0);
    }

    @Test
    void personWithScore_compareTo_sameScoreAndClass_ordersByPersonId() {
        PersonWithScore p1 = new PersonWithScore(PersonId.of(1L), 10.0, ClassResultShortName.of("H21"));
        PersonWithScore p2 = new PersonWithScore(PersonId.of(2L), 10.0, ClassResultShortName.of("H21"));

        assertThat(p1.compareTo(p2)).isLessThan(0);
    }

    // -------------------------------------------------------------------------
    // ClassResultScores.compareTo() — nach Klassenname
    // -------------------------------------------------------------------------

    @Test
    void classResultScores_compareTo_ordersByClassShortName() {
        ClassResultScores d21 = new ClassResultScores(ClassResultShortName.of("D21"), List.of());
        ClassResultScores h21 = new ClassResultScores(ClassResultShortName.of("H21"), List.of());

        assertThat(d21.compareTo(h21)).isLessThan(0);
        assertThat(h21.compareTo(d21)).isGreaterThan(0);
        assertThat(d21.compareTo(d21)).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // OrganisationScore.compareTo() — absteigend nach Score
    // -------------------------------------------------------------------------

    @Test
    void organisationScore_compareTo_sortsByScoreDescending() {
        OrganisationScore higher = new OrganisationScore(org("A"), 50.0, List.of());
        OrganisationScore lower = new OrganisationScore(org("B"), 30.0, List.of());

        assertThat(higher.compareTo(lower)).isLessThan(0);
        assertThat(lower.compareTo(higher)).isGreaterThan(0);
    }

    @Test
    void organisationScore_compareTo_sameScore_ordersByOrganisation() {
        OrganisationScore a = new OrganisationScore(org("A-Verein"), 10.0, List.of());
        OrganisationScore b = new OrganisationScore(org("B-Verein"), 10.0, List.of());

        assertThat(a.compareTo(b)).isLessThan(0);
    }

    // -------------------------------------------------------------------------
    // RaceClassResultGroupedCupScore / RaceOrganisationGroupedCupScore
    // -------------------------------------------------------------------------

    @Test
    void raceClassResultGroupedCupScore_compareTo_ordersByRace() {
        Race race1 = Race.of(EventId.of(1L), (byte) 1);
        Race race2 = Race.of(EventId.of(1L), (byte) 2);

        RaceClassResultGroupedCupScore r1 = new RaceClassResultGroupedCupScore(race1, List.of());
        RaceClassResultGroupedCupScore r2 = new RaceClassResultGroupedCupScore(race2, List.of());

        assertThat(r1.compareTo(r2)).isLessThan(0);
        assertThat(r2.compareTo(r1)).isGreaterThan(0);
        assertThat(r1.compareTo(r1)).isEqualTo(0);
    }

    @Test
    void raceOrganisationGroupedCupScore_compareTo_ordersByRace() {
        Race race1 = Race.of(EventId.of(1L), (byte) 1);
        Race race2 = Race.of(EventId.of(1L), (byte) 2);

        RaceOrganisationGroupedCupScore r1 = new RaceOrganisationGroupedCupScore(race1, List.of());
        RaceOrganisationGroupedCupScore r2 = new RaceOrganisationGroupedCupScore(race2, List.of());

        assertThat(r1.compareTo(r2)).isLessThan(0);
    }
}
