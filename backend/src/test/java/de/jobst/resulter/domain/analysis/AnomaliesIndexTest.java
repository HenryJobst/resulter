package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.application.analysis.SegmentKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for Anomalies Index (AI) calculation and classification.
 */
@DisplayName("Anomalies Index")
class AnomaliesIndexTest {

    @Nested
    @DisplayName("Creation and Calculation")
    class CreationAndCalculation {

        @Test
        @DisplayName("Should calculate AI when runner performs as expected")
        void shouldCalculateAI_WhenRunnerPerformsAsExpected() {
            // Given
            PerformanceIndex piReal = new PerformanceIndex(1.0);
            PerformanceIndex piExpected = new PerformanceIndex(1.0);
            SegmentKey segmentKey = new SegmentKey("M21", "101", "102");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(1, segmentKey, piReal, piExpected,
                AnomalyClassification.NO_SUSPICION, 100.0);

            // Then
            assertThat(ai.aiValue()).isEqualTo(1.0);
            assertThat(ai.classification()).isEqualTo(AnomalyClassification.NO_SUSPICION);
        }

        @Test
        @DisplayName("Should calculate AI when runner is faster than expected (suspicious)")
        void shouldCalculateAI_WhenRunnerIsFasterThanExpected() {
            // Given
            PerformanceIndex piReal = new PerformanceIndex(0.25); // Much faster
            PerformanceIndex piExpected = new PerformanceIndex(1.0);
            SegmentKey segmentKey = new SegmentKey("M21", "101", "102");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(1, segmentKey, piReal, piExpected,
                AnomalyClassification.HIGH_SUSPICION, 100.0);

            // Then
            assertThat(ai.aiValue()).isEqualTo(0.25); // AI < 1 indicates faster than expected
            assertThat(ai.classification()).isEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }

        @Test
        @DisplayName("Should calculate AI when runner is slower than expected")
        void shouldCalculateAI_WhenRunnerIsSlowerThanExpected() {
            // Given
            PerformanceIndex piReal = new PerformanceIndex(1.5); // Slower
            PerformanceIndex piExpected = new PerformanceIndex(1.0);
            SegmentKey segmentKey = new SegmentKey("M21", "101", "102");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(1, segmentKey, piReal, piExpected,
                AnomalyClassification.NO_SUSPICION, 100.0);

            // Then
            assertThat(ai.aiValue()).isEqualTo(1.5); // AI > 1 indicates slower than expected
            assertThat(ai.classification()).isEqualTo(AnomalyClassification.NO_SUSPICION);
        }

        @ParameterizedTest
        @CsvSource({
            "0.20, 1.00, 0.200, HIGH_SUSPICION",      // 80% faster than expected
            "0.25, 1.00, 0.250, HIGH_SUSPICION",      // 75% faster
            "0.30, 1.00, 0.300, HIGH_SUSPICION",      // 70% faster (threshold)
            "0.40, 1.00, 0.400, MODERATE_SUSPICION",  // 60% faster
            "0.45, 1.00, 0.450, MODERATE_SUSPICION",  // 55% faster (threshold)
            "0.50, 1.00, 0.500, MODERATE_SUSPICION",  // 50% faster
            "0.85, 1.00, 0.850, NO_SUSPICION",        // 15% faster (normal variation)
            "1.00, 1.00, 1.000, NO_SUSPICION",        // Same as expected
            "1.50, 1.00, 1.500, NO_SUSPICION",        // 50% slower (not suspicious)
        })
        @DisplayName("Should calculate correct AI for various performance scenarios")
        void shouldCalculateCorrectAI(double piRealValue, double piExpectedValue,
                                      double expectedAI, AnomalyClassification classification) {
            // Given
            PerformanceIndex piReal = new PerformanceIndex(piRealValue);
            PerformanceIndex piExpected = new PerformanceIndex(piExpectedValue);
            SegmentKey segmentKey = new SegmentKey("M21", "101", "102");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(1, segmentKey, piReal, piExpected, classification, 100.0);

            // Then
            assertThat(ai.aiValue()).isCloseTo(expectedAI, within(0.001));
        }

        @Test
        @DisplayName("Should handle elite runner with low expected PI")
        void shouldHandleEliteRunnerWithLowExpectedPI() {
            // Given - Elite runner (expected PI 0.9) runs even faster (PI 0.3)
            PerformanceIndex piReal = new PerformanceIndex(0.3);
            PerformanceIndex piExpected = new PerformanceIndex(0.9);
            SegmentKey segmentKey = new SegmentKey("M21", "101", "102");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(1, segmentKey, piReal, piExpected,
                AnomalyClassification.MODERATE_SUSPICION, 100.0);

            // Then
            assertThat(ai.aiValue()).isCloseTo(0.333, within(0.001));
        }

        @Test
        @DisplayName("Should handle average runner with high expected PI")
        void shouldHandleAverageRunnerWithHighExpectedPI() {
            // Given - Average runner (expected PI 1.3) runs much faster (PI 0.4)
            PerformanceIndex piReal = new PerformanceIndex(0.4);
            PerformanceIndex piExpected = new PerformanceIndex(1.3);
            SegmentKey segmentKey = new SegmentKey("M21", "101", "102");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(1, segmentKey, piReal, piExpected,
                AnomalyClassification.HIGH_SUSPICION, 100.0);

            // Then
            assertThat(ai.aiValue()).isCloseTo(0.308, within(0.001));
        }
    }

    @Nested
    @DisplayName("Classification")
    class Classification {

        @Test
        @DisplayName("Should classify as NO_SUSPICION for normal performance")
        void shouldClassifyAsNoSuspicion_ForNormalPerformance() {
            // Given
            PerformanceIndex piReal = new PerformanceIndex(0.95);
            PerformanceIndex piExpected = new PerformanceIndex(1.0);
            SegmentKey segmentKey = new SegmentKey("M21", "101", "102");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(1, segmentKey, piReal, piExpected,
                AnomalyClassification.NO_SUSPICION, 100.0);

            // Then
            assertThat(ai.classification()).isEqualTo(AnomalyClassification.NO_SUSPICION);
        }

        @Test
        @DisplayName("Should classify as MODERATE_SUSPICION for moderately fast performance")
        void shouldClassifyAsModerateSuspicion_ForModeratelyFastPerformance() {
            // Given
            PerformanceIndex piReal = new PerformanceIndex(0.40);
            PerformanceIndex piExpected = new PerformanceIndex(1.0);
            SegmentKey segmentKey = new SegmentKey("M21", "101", "102");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(1, segmentKey, piReal, piExpected,
                AnomalyClassification.MODERATE_SUSPICION, 100.0);

            // Then
            assertThat(ai.classification()).isEqualTo(AnomalyClassification.MODERATE_SUSPICION);
        }

        @Test
        @DisplayName("Should classify as HIGH_SUSPICION for extremely fast performance")
        void shouldClassifyAsHighSuspicion_ForExtremelyFastPerformance() {
            // Given
            PerformanceIndex piReal = new PerformanceIndex(0.25);
            PerformanceIndex piExpected = new PerformanceIndex(1.0);
            SegmentKey segmentKey = new SegmentKey("M21", "101", "102");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(1, segmentKey, piReal, piExpected,
                AnomalyClassification.HIGH_SUSPICION, 100.0);

            // Then
            assertThat(ai.classification()).isEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }

        @Test
        @DisplayName("Should classify as NO_DATA when insufficient data")
        void shouldClassifyAsNoData_WhenInsufficientData() {
            // Given
            PerformanceIndex piReal = new PerformanceIndex(0.0);
            PerformanceIndex piExpected = new PerformanceIndex(1.0);
            SegmentKey segmentKey = new SegmentKey("M21", "101", "102");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(1, segmentKey, piReal, piExpected,
                AnomalyClassification.NO_DATA, 100.0);

            // Then
            assertThat(ai.classification()).isEqualTo(AnomalyClassification.NO_DATA);
        }
    }

    @Nested
    @DisplayName("Segment Information")
    class SegmentInformation {

        @Test
        @DisplayName("Should store leg number correctly")
        void shouldStoreLegNumberCorrectly() {
            // Given
            PerformanceIndex piReal = new PerformanceIndex(1.0);
            PerformanceIndex piExpected = new PerformanceIndex(1.0);
            SegmentKey segmentKey = new SegmentKey("M21", "101", "102");
            int legNumber = 5;

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(legNumber, segmentKey, piReal, piExpected,
                AnomalyClassification.NO_SUSPICION, 100.0);

            // Then
            assertThat(ai.legNumber()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should store segment key with class information")
        void shouldStoreSegmentKeyWithClassInformation() {
            // Given
            PerformanceIndex piReal = new PerformanceIndex(1.0);
            PerformanceIndex piExpected = new PerformanceIndex(1.0);
            SegmentKey segmentKey = new SegmentKey("M21", "101", "102");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(1, segmentKey, piReal, piExpected,
                AnomalyClassification.NO_SUSPICION, 100.0);

            // Then
            assertThat(ai.segmentKey().className()).isEqualTo("M21");
            assertThat(ai.segmentKey().fromControl()).isEqualTo("101");
            assertThat(ai.segmentKey().toControl()).isEqualTo("102");
        }

        @Test
        @DisplayName("Should handle cross-class segment key (null className)")
        void shouldHandleCrossClassSegmentKey() {
            // Given
            PerformanceIndex piReal = new PerformanceIndex(1.0);
            PerformanceIndex piExpected = new PerformanceIndex(1.0);
            SegmentKey segmentKey = new SegmentKey(null, "101", "102");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(1, segmentKey, piReal, piExpected,
                AnomalyClassification.NO_SUSPICION, 100.0);

            // Then
            assertThat(ai.segmentKey().className()).isNull();
            assertThat(ai.segmentKey().fromControl()).isEqualTo("101");
            assertThat(ai.segmentKey().toControl()).isEqualTo("102");
        }
    }

    @Nested
    @DisplayName("Real-World Scenarios")
    class RealWorldScenarios {

        @Test
        @DisplayName("Should detect shortcut on road segment")
        void shouldDetectShortcutOnRoadSegment() {
            // Given - Runner takes shortcut on 300s road segment
            // Reference: 300s, Runner time: 80s (73% faster)
            PerformanceIndex piReal = new PerformanceIndex(0.27); // 80s / 300s
            PerformanceIndex piExpected = new PerformanceIndex(1.0);
            SegmentKey segmentKey = new SegmentKey("M21", "103", "104");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(3, segmentKey, piReal, piExpected,
                AnomalyClassification.HIGH_SUSPICION, 100.0);

            // Then
            assertThat(ai.aiValue()).isCloseTo(0.27, within(0.01));
            assertThat(ai.classification()).isEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }

        @Test
        @DisplayName("Should NOT flag exceptional elite performance")
        void shouldNotFlagExceptionalElitePerformance() {
            // Given - Elite runner on good day (20% faster than their normal)
            PerformanceIndex piReal = new PerformanceIndex(0.72); // 20% faster
            PerformanceIndex piExpected = new PerformanceIndex(0.9); // Elite baseline
            SegmentKey segmentKey = new SegmentKey("M21", "105", "106");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(5, segmentKey, piReal, piExpected,
                AnomalyClassification.NO_SUSPICION, 100.0);

            // Then
            assertThat(ai.aiValue()).isCloseTo(0.80, within(0.01));
            assertThat(ai.classification()).isEqualTo(AnomalyClassification.NO_SUSPICION);
        }

        @Test
        @DisplayName("Should detect systematic anomaly across segments")
        void shouldDetectSystematicAnomalyAcrossSegments() {
            // Given - Runner consistently 65% faster than baseline
            PerformanceIndex piReal = new PerformanceIndex(0.35);
            PerformanceIndex piExpected = new PerformanceIndex(1.0);
            SegmentKey segmentKey1 = new SegmentKey("M21", "101", "102");
            SegmentKey segmentKey2 = new SegmentKey("M21", "102", "103");

            // When
            AnomaliesIndex ai1 = AnomaliesIndex.of(1, segmentKey1, piReal, piExpected,
                AnomalyClassification.HIGH_SUSPICION, 100.0);
            AnomaliesIndex ai2 = AnomaliesIndex.of(2, segmentKey2, piReal, piExpected,
                AnomalyClassification.HIGH_SUSPICION, 100.0);

            // Then
            assertThat(ai1.aiValue()).isEqualTo(0.35);
            assertThat(ai2.aiValue()).isEqualTo(0.35);
            assertThat(ai1.classification()).isEqualTo(AnomalyClassification.HIGH_SUSPICION);
            assertThat(ai2.classification()).isEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }

        @Test
        @DisplayName("Should handle runner who makes mistake then cheats")
        void shouldHandleRunnerWhoMakesMistakeThenCheats() {
            // Given - Runner with worse-than-expected PI (made mistakes earlier)
            PerformanceIndex piReal = new PerformanceIndex(0.28); // Still very fast
            PerformanceIndex piExpected = new PerformanceIndex(1.3); // Poor baseline
            SegmentKey segmentKey = new SegmentKey("M21", "107", "108");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(7, segmentKey, piReal, piExpected,
                AnomalyClassification.HIGH_SUSPICION, 100.0);

            // Then
            assertThat(ai.aiValue()).isCloseTo(0.215, within(0.001));
            assertThat(ai.classification()).isEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }
    }

    @Nested
    @DisplayName("Boundary Conditions")
    class BoundaryConditions {

        @Test
        @DisplayName("Should handle AI exactly at HIGH_SUSPICION threshold (0.30)")
        void shouldHandleAIAtHighSuspicionThreshold() {
            // Given
            PerformanceIndex piReal = new PerformanceIndex(0.30);
            PerformanceIndex piExpected = new PerformanceIndex(1.0);
            SegmentKey segmentKey = new SegmentKey("M21", "101", "102");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(1, segmentKey, piReal, piExpected,
                AnomalyClassification.HIGH_SUSPICION, 100.0);

            // Then
            assertThat(ai.aiValue()).isEqualTo(0.30);
        }

        @Test
        @DisplayName("Should handle AI exactly at MODERATE_SUSPICION threshold (0.45)")
        void shouldHandleAIAtModerateSuspicionThreshold() {
            // Given
            PerformanceIndex piReal = new PerformanceIndex(0.45);
            PerformanceIndex piExpected = new PerformanceIndex(1.0);
            SegmentKey segmentKey = new SegmentKey("M21", "101", "102");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(1, segmentKey, piReal, piExpected,
                AnomalyClassification.MODERATE_SUSPICION, 100.0);

            // Then
            assertThat(ai.aiValue()).isEqualTo(0.45);
        }

        @Test
        @DisplayName("Should handle very small PI values")
        void shouldHandleVerySmallPIValues() {
            // Given - Extremely fast (physically impossible)
            PerformanceIndex piReal = new PerformanceIndex(0.05);
            PerformanceIndex piExpected = new PerformanceIndex(1.0);
            SegmentKey segmentKey = new SegmentKey("M21", "101", "102");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(1, segmentKey, piReal, piExpected,
                AnomalyClassification.HIGH_SUSPICION, 100.0);

            // Then
            assertThat(ai.aiValue()).isEqualTo(0.05);
            assertThat(ai.classification()).isEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }

        @Test
        @DisplayName("Should handle very large PI values")
        void shouldHandleVeryLargePIValues() {
            // Given - Very slow runner
            PerformanceIndex piReal = new PerformanceIndex(3.0);
            PerformanceIndex piExpected = new PerformanceIndex(1.0);
            SegmentKey segmentKey = new SegmentKey("M21", "101", "102");

            // When
            AnomaliesIndex ai = AnomaliesIndex.of(1, segmentKey, piReal, piExpected,
                AnomalyClassification.NO_SUSPICION, 100.0);

            // Then
            assertThat(ai.aiValue()).isEqualTo(3.0);
            assertThat(ai.classification()).isEqualTo(AnomalyClassification.NO_SUSPICION);
        }
    }
}
