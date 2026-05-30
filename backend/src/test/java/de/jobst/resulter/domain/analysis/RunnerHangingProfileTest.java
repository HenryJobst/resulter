package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.ControlCode;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.PunchTime;
import de.jobst.resulter.domain.RaceNumber;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RunnerHangingProfileTest {

    private static HangingPair pair(int legNumber, String from, String to, double hiValue) {
        return new HangingPair(
                legNumber,
                ControlCode.of(from),
                ControlCode.of(to),
                PersonId.of(99L),
                "H21",
                RaceNumber.of((byte) 1),
                15.0,
                new PerformanceIndex(1.10),
                new PerformanceIndex(1.00),
                new HangingIndex(hiValue),
                110.0,
                100.0,
                100.0
        );
    }

    private static RunnerHangingProfile profile(List<HangingPair> pairs, int totalNonMistakeSegments) {
        return new RunnerHangingProfile(
                PersonId.of(1L),
                "H21",
                RaceNumber.of((byte) 1),
                PunchTime.of(0.0),
                10,
                true,
                new PerformanceIndex(1.10),
                pairs,
                pairs.stream().mapToDouble(p -> p.hangingIndex().value()).average().orElse(0.0),
                HangingClassification.MODERATE_HANGING,
                totalNonMistakeSegments
        );
    }

    // -------------------------------------------------------------------------
    // getHangingCount — unique Segmente
    // -------------------------------------------------------------------------

    @Test
    void getHangingCount_returnsZeroWhenNoPairs() {
        RunnerHangingProfile p = profile(List.of(), 5);
        assertThat(p.getHangingCount()).isEqualTo(0);
    }

    @Test
    void getHangingCount_countsSinglePair() {
        RunnerHangingProfile p = profile(List.of(pair(1, "31", "32", 0.80)), 5);
        assertThat(p.getHangingCount()).isEqualTo(1);
    }

    @Test
    void getHangingCount_deduplicatesSameSegment() {
        // Two pairs on same segment leg1/31→32 should count as 1 unique segment
        List<HangingPair> pairs = List.of(
                pair(1, "31", "32", 0.80),
                pair(1, "31", "32", 0.75)
        );
        RunnerHangingProfile p = profile(pairs, 5);
        assertThat(p.getHangingCount()).isEqualTo(1);
    }

    @Test
    void getHangingCount_countsDifferentSegmentsSeparately() {
        List<HangingPair> pairs = List.of(
                pair(1, "31", "32", 0.80),
                pair(2, "32", "33", 0.75)
        );
        RunnerHangingProfile p = profile(pairs, 5);
        assertThat(p.getHangingCount()).isEqualTo(2);
    }

    // -------------------------------------------------------------------------
    // getHangingPercentage
    // -------------------------------------------------------------------------

    @Test
    void getHangingPercentage_returnsZeroWhenNoSegments() {
        RunnerHangingProfile p = profile(List.of(), 0);
        assertThat(p.getHangingPercentage()).isEqualTo(0.0);
    }

    @Test
    void getHangingPercentage_calculatesCorrectly() {
        // 1 unique hanging segment out of 4 total → 25%
        RunnerHangingProfile p = profile(List.of(pair(1, "31", "32", 0.80)), 4);
        assertThat(p.getHangingPercentage()).isEqualTo(25.0);
    }

    // -------------------------------------------------------------------------
    // hasHanging
    // -------------------------------------------------------------------------

    @Test
    void hasHanging_returnsFalseWhenNoPairs() {
        RunnerHangingProfile p = profile(List.of(), 5);
        assertThat(p.hasHanging()).isFalse();
    }

    @Test
    void hasHanging_returnsTrueWithPairs() {
        RunnerHangingProfile p = profile(List.of(pair(1, "31", "32", 0.80)), 5);
        assertThat(p.hasHanging()).isTrue();
    }

    // -------------------------------------------------------------------------
    // getMinimumHangingIndex
    // -------------------------------------------------------------------------

    @Test
    void getMinimumHangingIndex_returnsNullWhenNoPairs() {
        RunnerHangingProfile p = profile(List.of(), 5);
        assertThat(p.getMinimumHangingIndex()).isNull();
    }

    @Test
    void getMinimumHangingIndex_returnsLowestValue() {
        List<HangingPair> pairs = List.of(
                pair(1, "31", "32", 0.80),
                pair(2, "32", "33", 0.70),
                pair(3, "33", "34", 0.75)
        );
        RunnerHangingProfile p = profile(pairs, 5);
        assertThat(p.getMinimumHangingIndex()).isEqualTo(0.70);
    }

    // -------------------------------------------------------------------------
    // getMaximumImprovement
    // -------------------------------------------------------------------------

    @Test
    void getMaximumImprovement_returnsZeroWhenNoPairs() {
        RunnerHangingProfile p = profile(List.of(), 5);
        assertThat(p.getMaximumImprovement()).isEqualTo(0.0);
    }

    @Test
    void getMaximumImprovement_returnsHighestImprovement() {
        // HI=0.70 → 30% improvement; HI=0.80 → 20%
        List<HangingPair> pairs = List.of(
                pair(1, "31", "32", 0.80),
                pair(2, "32", "33", 0.70)
        );
        RunnerHangingProfile p = profile(pairs, 5);
        assertThat(p.getMaximumImprovement()).isCloseTo(30.0, org.assertj.core.data.Offset.offset(1e-9));
    }

    // -------------------------------------------------------------------------
    // compareTo
    // -------------------------------------------------------------------------

    @Test
    void compareTo_ordersByAverageHangingIndexAscending() {
        RunnerHangingProfile moreHanging = new RunnerHangingProfile(
                PersonId.of(1L), "H21", RaceNumber.of((byte) 1), PunchTime.of(0.0),
                10, true, new PerformanceIndex(1.10), List.of(), 0.70,
                HangingClassification.HIGH_HANGING, 5);
        RunnerHangingProfile lessHanging = new RunnerHangingProfile(
                PersonId.of(2L), "H21", RaceNumber.of((byte) 1), PunchTime.of(0.0),
                10, true, new PerformanceIndex(1.10), List.of(), 0.85,
                HangingClassification.MODERATE_HANGING, 5);

        assertThat(moreHanging.compareTo(lessHanging)).isLessThan(0);
        assertThat(lessHanging.compareTo(moreHanging)).isGreaterThan(0);
        assertThat(moreHanging.compareTo(moreHanging)).isEqualTo(0);
    }

    @Test
    void toString_containsPersonIdAndClass() {
        RunnerHangingProfile p = profile(List.of(pair(1, "31", "32", 0.80)), 5);
        String s = p.toString();
        assertThat(s).contains("PersonId=1").contains("H21");
    }
}
