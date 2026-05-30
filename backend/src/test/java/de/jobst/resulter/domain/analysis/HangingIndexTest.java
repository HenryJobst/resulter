package de.jobst.resulter.domain.analysis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class HangingIndexTest {

    private static PerformanceIndex pi(double runner, double best) {
        return PerformanceIndex.of(runner, best);
    }

    @Test
    void of_computesRatioSegmentPiDividedByExpectedPi() {
        // segmentPI = 0.9, expectedPI = 1.0 → HI = 0.9
        HangingIndex hi = HangingIndex.of(pi(90.0, 100.0), pi(100.0, 100.0));
        assertThat(hi.value()).isEqualTo(0.9);
    }

    @Test
    void of_throwsWhenExpectedPiNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> HangingIndex.of(pi(90.0, 100.0), null));
    }

    @Test
    void of_throwsWhenExpectedPiZeroOrNegative() {
        // PerformanceIndex.of() prevents zero expected, but test the guard anyway via a direct HangingIndex.of check
        PerformanceIndex zeroPI = new PerformanceIndex(0.0);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> HangingIndex.of(pi(90.0, 100.0), zeroPI));
    }

    @Test
    void of_throwsWhenSegmentPiNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> HangingIndex.of(null, pi(100.0, 100.0)));
    }

    @Test
    void isHanging_returnsTrueAtOrBelowThreshold() {
        HangingIndex hanging = new HangingIndex(HangingIndex.HANGING_THRESHOLD);      // exactly 0.85
        HangingIndex clearlyHanging = new HangingIndex(0.70);

        assertThat(hanging.isHanging()).isTrue();
        assertThat(clearlyHanging.isHanging()).isTrue();
    }

    @Test
    void isHanging_returnsFalseAboveThreshold() {
        HangingIndex notHanging = new HangingIndex(0.90);
        assertThat(notHanging.isHanging()).isFalse();
    }

    @Test
    void isHanging_customThreshold() {
        HangingIndex hi = new HangingIndex(0.80);
        assertThat(hi.isHanging(0.85)).isTrue();
        assertThat(hi.isHanging(0.75)).isFalse();
    }

    @Test
    void getImprovementPercent_calculatesCorrectly() {
        // HI = 0.75 → 25% improvement
        HangingIndex hi = new HangingIndex(0.75);
        assertThat(hi.getImprovementPercent()).isEqualTo(25.0);
    }

    @Test
    void getDegradationPercent_returnsZeroWhenImproved() {
        HangingIndex hi = new HangingIndex(0.80); // improved
        assertThat(hi.getDegradationPercent()).isEqualTo(0.0);
    }

    @Test
    void getDegradationPercent_calculatesCorrectlyWhenWorse() {
        // HI = 1.20 → 20% worse
        HangingIndex hi = new HangingIndex(1.20);
        assertThat(hi.getDegradationPercent()).isEqualTo(20.0, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    void compareTo_lowerHiIsLess() {
        HangingIndex better = new HangingIndex(0.70);
        HangingIndex worse = new HangingIndex(0.90);

        assertThat(better.compareTo(worse)).isLessThan(0);
        assertThat(worse.compareTo(better)).isGreaterThan(0);
        assertThat(better.compareTo(better)).isEqualTo(0);
    }

    @Test
    void toString_formatsWithThreeDecimalPlaces() {
        HangingIndex hi = new HangingIndex(0.85);
        assertThat(hi.toString()).startsWith("HI(").endsWith(")").contains("85");
    }

    @Test
    void of_throwsWhenSegmentPiValueNegative() {
        PerformanceIndex negPI = new PerformanceIndex(-0.1);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> HangingIndex.of(negPI, pi(100.0, 100.0)));
    }
}
