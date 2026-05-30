package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.ControlCode;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.RaceNumber;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class HangingPairTest {

    private static HangingPair validPair() {
        return new HangingPair(
                1,
                ControlCode.of("31"),
                ControlCode.of("32"),
                PersonId.of(99L),
                "H21",
                RaceNumber.of((byte) 1),
                15.0,
                new PerformanceIndex(1.10),
                new PerformanceIndex(1.00),
                new HangingIndex(0.80),
                110.0,
                100.0,
                100.0
        );
    }

    // -------------------------------------------------------------------------
    // Validierung im Compact Constructor
    // -------------------------------------------------------------------------

    @Test
    void constructor_throwsWhenLegNumberZero() {
        assertThatIllegalArgumentException().isThrownBy(() -> new HangingPair(
                0, ControlCode.of("31"), ControlCode.of("32"), PersonId.of(1L),
                "H21", RaceNumber.of((byte) 1), 15.0,
                new PerformanceIndex(1.10), new PerformanceIndex(1.00),
                new HangingIndex(0.80), 110.0, 100.0, 100.0));
    }

    @Test
    void constructor_throwsWhenFromControlNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> new HangingPair(
                1, null, ControlCode.of("32"), PersonId.of(1L),
                "H21", RaceNumber.of((byte) 1), 15.0,
                new PerformanceIndex(1.10), new PerformanceIndex(1.00),
                new HangingIndex(0.80), 110.0, 100.0, 100.0));
    }

    @Test
    void constructor_throwsWhenToControlNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> new HangingPair(
                1, ControlCode.of("31"), null, PersonId.of(1L),
                "H21", RaceNumber.of((byte) 1), 15.0,
                new PerformanceIndex(1.10), new PerformanceIndex(1.00),
                new HangingIndex(0.80), 110.0, 100.0, 100.0));
    }

    @Test
    void constructor_throwsWhenBusDriverIdNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> new HangingPair(
                1, ControlCode.of("31"), ControlCode.of("32"), null,
                "H21", RaceNumber.of((byte) 1), 15.0,
                new PerformanceIndex(1.10), new PerformanceIndex(1.00),
                new HangingIndex(0.80), 110.0, 100.0, 100.0));
    }

    @Test
    void constructor_throwsWhenBusDriverClassNameBlank() {
        assertThatIllegalArgumentException().isThrownBy(() -> new HangingPair(
                1, ControlCode.of("31"), ControlCode.of("32"), PersonId.of(1L),
                "  ", RaceNumber.of((byte) 1), 15.0,
                new PerformanceIndex(1.10), new PerformanceIndex(1.00),
                new HangingIndex(0.80), 110.0, 100.0, 100.0));
    }

    @Test
    void constructor_throwsWhenTimeDeltaExceedsMax() {
        assertThatIllegalArgumentException().isThrownBy(() -> new HangingPair(
                1, ControlCode.of("31"), ControlCode.of("32"), PersonId.of(1L),
                "H21", RaceNumber.of((byte) 1), 30.01,
                new PerformanceIndex(1.10), new PerformanceIndex(1.00),
                new HangingIndex(0.80), 110.0, 100.0, 100.0));
    }

    @Test
    void constructor_throwsWhenTimeDeltaNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> new HangingPair(
                1, ControlCode.of("31"), ControlCode.of("32"), PersonId.of(1L),
                "H21", RaceNumber.of((byte) 1), -1.0,
                new PerformanceIndex(1.10), new PerformanceIndex(1.00),
                new HangingIndex(0.80), 110.0, 100.0, 100.0));
    }

    @Test
    void constructor_acceptsMaxTimeDelta() {
        HangingPair pair = new HangingPair(
                1, ControlCode.of("31"), ControlCode.of("32"), PersonId.of(1L),
                "H21", RaceNumber.of((byte) 1), 30.0,
                new PerformanceIndex(1.10), new PerformanceIndex(1.00),
                new HangingIndex(0.80), 110.0, 100.0, 100.0);
        assertThat(pair.timeDeltaSeconds()).isEqualTo(30.0);
    }

    @Test
    void constructor_throwsWhenPassengerTimeNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> new HangingPair(
                1, ControlCode.of("31"), ControlCode.of("32"), PersonId.of(1L),
                "H21", RaceNumber.of((byte) 1), 15.0,
                new PerformanceIndex(1.10), new PerformanceIndex(1.00),
                new HangingIndex(0.80), -1.0, 100.0, 100.0));
    }

    @Test
    void constructor_throwsWhenBusDriverTimeNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> new HangingPair(
                1, ControlCode.of("31"), ControlCode.of("32"), PersonId.of(1L),
                "H21", RaceNumber.of((byte) 1), 15.0,
                new PerformanceIndex(1.10), new PerformanceIndex(1.00),
                new HangingIndex(0.80), 110.0, -1.0, 100.0));
    }

    @Test
    void constructor_throwsWhenReferenceTimeZero() {
        assertThatIllegalArgumentException().isThrownBy(() -> new HangingPair(
                1, ControlCode.of("31"), ControlCode.of("32"), PersonId.of(1L),
                "H21", RaceNumber.of((byte) 1), 15.0,
                new PerformanceIndex(1.10), new PerformanceIndex(1.00),
                new HangingIndex(0.80), 110.0, 100.0, 0.0));
    }

    // -------------------------------------------------------------------------
    // Berechnete Methoden
    // -------------------------------------------------------------------------

    @Test
    void getImprovementPercent_delegatesToHangingIndex() {
        // HI = 0.75 → 25% improvement
        HangingPair pair = new HangingPair(
                1, ControlCode.of("31"), ControlCode.of("32"), PersonId.of(1L),
                "H21", RaceNumber.of((byte) 1), 10.0,
                new PerformanceIndex(1.10), new PerformanceIndex(1.00),
                new HangingIndex(0.75), 110.0, 100.0, 100.0);
        assertThat(pair.getImprovementPercent()).isEqualTo(25.0);
    }

    @Test
    void isBusDriverFaster_returnsTrueWhenBusDriverHasLowerPI() {
        HangingPair pair = validPair(); // passenger=1.10, busDriver=1.00
        assertThat(pair.isBusDriverFaster()).isTrue();
    }

    @Test
    void isBusDriverFaster_returnsFalseWhenPassengerHasLowerPI() {
        HangingPair pair = new HangingPair(
                1, ControlCode.of("31"), ControlCode.of("32"), PersonId.of(1L),
                "H21", RaceNumber.of((byte) 1), 10.0,
                new PerformanceIndex(1.00), new PerformanceIndex(1.20),
                new HangingIndex(0.80), 100.0, 120.0, 100.0);
        assertThat(pair.isBusDriverFaster()).isFalse();
    }

    @Test
    void getTimeDifference_returnsPassengerMinusBusDriver() {
        HangingPair pair = validPair(); // passenger=110.0, busDriver=100.0
        assertThat(pair.getTimeDifference()).isEqualTo(10.0);
    }

    @Test
    void constructor_throwsWhenBusDriverClassNameNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> new HangingPair(
                1, ControlCode.of("31"), ControlCode.of("32"), PersonId.of(1L),
                null, RaceNumber.of((byte) 1), 15.0,
                new PerformanceIndex(1.10), new PerformanceIndex(1.00),
                new HangingIndex(0.80), 110.0, 100.0, 100.0));
    }

    @Test
    void constructor_throwsWhenBusDriverRaceNumberNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> new HangingPair(
                1, ControlCode.of("31"), ControlCode.of("32"), PersonId.of(1L),
                "H21", null, 15.0,
                new PerformanceIndex(1.10), new PerformanceIndex(1.00),
                new HangingIndex(0.80), 110.0, 100.0, 100.0));
    }

    @Test
    void constructor_throwsWhenPassengerSegmentPINull() {
        assertThatIllegalArgumentException().isThrownBy(() -> new HangingPair(
                1, ControlCode.of("31"), ControlCode.of("32"), PersonId.of(1L),
                "H21", RaceNumber.of((byte) 1), 15.0,
                null, new PerformanceIndex(1.00),
                new HangingIndex(0.80), 110.0, 100.0, 100.0));
    }

    @Test
    void constructor_throwsWhenBusDriverSegmentPINull() {
        assertThatIllegalArgumentException().isThrownBy(() -> new HangingPair(
                1, ControlCode.of("31"), ControlCode.of("32"), PersonId.of(1L),
                "H21", RaceNumber.of((byte) 1), 15.0,
                new PerformanceIndex(1.10), null,
                new HangingIndex(0.80), 110.0, 100.0, 100.0));
    }

    @Test
    void constructor_throwsWhenHangingIndexNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> new HangingPair(
                1, ControlCode.of("31"), ControlCode.of("32"), PersonId.of(1L),
                "H21", RaceNumber.of((byte) 1), 15.0,
                new PerformanceIndex(1.10), new PerformanceIndex(1.00),
                null, 110.0, 100.0, 100.0));
    }
}
