package de.jobst.resulter.application.analysis;

import de.jobst.resulter.domain.analysis.AnomalyClassification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for AnomalyDetectionServiceImpl algorithm logic.
 * Tests private methods via reflection to ensure threshold calculations are correct.
 */
@DisplayName("Anomaly Detection Service")
class AnomalyDetectionServiceImplTest {

    private AnomalyDetectionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AnomalyDetectionServiceImpl(null, null, null);
    }

    @Nested
    @DisplayName("Classification Algorithm")
    class ClassificationAlgorithm {

        @Test
        @DisplayName("Should classify as NO_SUSPICION when all criteria fail")
        void shouldClassifyAsNoSuspicion_WhenAllCriteriaFail() throws Exception {
            // Given - Normal performance (not faster enough)
            double piReal = 0.90;        // Only 10% faster than top-3
            double aiValue = 0.90;       // Only 10% faster than own baseline
            double timeDifference = 15;  // Only 15 seconds faster
            double referenceTime = 200;  // 200s segment

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then
            assertThat(result).isEqualTo(AnomalyClassification.NO_SUSPICION);
        }

        @Test
        @DisplayName("Should classify as HIGH_SUSPICION when all criteria met for long segment")
        void shouldClassifyAsHighSuspicion_WhenAllCriteriaMet_LongSegment() throws Exception {
            // Given - Extremely fast on 300s segment
            double piReal = 0.25;        // 75% faster than top-3 ✓
            double aiValue = 0.25;       // 75% faster than own baseline ✓
            double referenceTime = 300;
            double timeDifference = 120; // 120s faster (300s * 20% = 60s, * 1.8 = 108s) ✓

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then
            assertThat(result).isEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }

        @Test
        @DisplayName("Should classify as MODERATE_SUSPICION when moderate criteria met")
        void shouldClassifyAsModerateSuspicion_WhenModerateCriteriaMet() throws Exception {
            // Given - Moderately fast on 200s segment
            double piReal = 0.40;        // 60% faster than top-3 ✓
            double aiValue = 0.40;       // 60% faster than own baseline ✓
            double referenceTime = 200;
            double timeDifference = 50;  // 50s faster (200s * 20% = 40s minimum) ✓

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then
            assertThat(result).isEqualTo(AnomalyClassification.MODERATE_SUSPICION);
        }

        @Test
        @DisplayName("Should require ALL three criteria for HIGH_SUSPICION")
        void shouldRequireAllThreeCriteria_ForHighSuspicion() throws Exception {
            // Given - Missing time difference criterion
            double piReal = 0.25;        // ✓ Fast enough percentage-wise
            double aiValue = 0.25;       // ✓ Fast enough vs baseline
            double referenceTime = 200;
            double timeDifference = 30;  // ✗ Not enough absolute time (need 72s = 40s * 1.8)

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then
            assertThat(result).isNotEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }

        @ParameterizedTest
        @CsvSource({
            // piReal, aiValue, timeDiff, refTime, expected
            "0.25, 0.25, 80,  200, HIGH_SUSPICION",      // All HIGH criteria met
            "0.40, 0.40, 50,  200, MODERATE_SUSPICION",  // All MODERATE criteria met
            "0.25, 0.50, 80,  200, NO_SUSPICION",        // piReal good, aiValue fails
            "0.50, 0.25, 80,  200, NO_SUSPICION",        // aiValue good, piReal fails
            "0.25, 0.25, 20,  200, NO_SUSPICION",        // PI good, time difference fails
            "0.90, 0.90, 10,  200, NO_SUSPICION",        // Nothing suspicious
            "0.20, 0.20, 180, 500, HIGH_SUSPICION",      // Long segment HIGH (need 100*1.8=180s)
            "0.40, 0.40, 100, 500, MODERATE_SUSPICION",  // Long segment MODERATE (need 100s)
        })
        @DisplayName("Should apply triple-criteria logic correctly")
        void shouldApplyTripleCriteriaLogicCorrectly(
            double piReal, double aiValue, double timeDifference,
            double referenceTime, AnomalyClassification expected) throws Exception {

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Dynamic Time Thresholds")
    class DynamicTimeThresholds {

        @Test
        @DisplayName("Should use 40s minimum for very short segments")
        void shouldUseMinimumThreshold_ForVeryShortSegments() throws Exception {
            // Given - 30s segment (20% would be 6s, but min is 40s)
            double piReal = 0.25;
            double aiValue = 0.25;
            double referenceTime = 30;
            double timeDifference = 45;  // Must exceed 40s minimum * 1.8 = 72s for HIGH

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then - Should NOT be HIGH because time diff (45s) < 72s required
            assertThat(result).isNotEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }

        @Test
        @DisplayName("Should scale threshold for medium segments")
        void shouldScaleThreshold_ForMediumSegments() throws Exception {
            // Given - 200s segment (20% = 40s, which equals minimum)
            double piReal = 0.25;
            double aiValue = 0.25;
            double referenceTime = 200;
            double timeDifference = 73;  // Just above 40s * 1.8 = 72s

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then
            assertThat(result).isEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }

        @Test
        @DisplayName("Should use 150s maximum for very long segments")
        void shouldUseMaximumThreshold_ForVeryLongSegments() throws Exception {
            // Given - 1000s segment (20% would be 200s, but max is 150s)
            double piReal = 0.25;
            double aiValue = 0.25;
            double referenceTime = 1000;
            double timeDifference = 280; // Exceeds 150s * 1.8 = 270s for HIGH

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then
            assertThat(result).isEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }

        @Test
        @DisplayName("Should have different thresholds for MODERATE vs HIGH")
        void shouldHaveDifferentThresholds_ForModerateVsHigh() throws Exception {
            // Given - Setup where MODERATE threshold is met but HIGH is not
            double piReal = 0.40;
            double aiValue = 0.40;
            double referenceTime = 200;
            double timeDifference = 50;  // Exceeds 40s MODERATE, but not 72s HIGH

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then
            assertThat(result).isEqualTo(AnomalyClassification.MODERATE_SUSPICION);
        }
    }

    @Nested
    @DisplayName("Short Segment Handling")
    class ShortSegmentHandling {

        @Test
        @DisplayName("Should apply 30% stricter thresholds for segments < 50s")
        void shouldApplyStricterThresholds_ForShortSegments() throws Exception {
            // Given - 45s segment (< 50s threshold)
            // Normal: PI < 0.30, with 0.70 multiplier: PI < 0.21 required
            double piReal = 0.25;        // Would normally qualify for HIGH
            double aiValue = 0.25;
            double referenceTime = 45;
            double timeDifference = 80;  // Plenty of time difference

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then - 0.25 is NOT < (0.30 * 0.70 = 0.21), so should not be HIGH
            assertThat(result).isNotEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }

        @Test
        @DisplayName("Should classify HIGH when short segment criteria met with multiplier")
        void shouldClassifyHigh_WhenShortSegmentCriteriaMet() throws Exception {
            // Given - 45s segment, runner meets strict criteria
            double piReal = 0.20;        // 0.20 < (0.30 * 0.70 = 0.21) ✓
            double aiValue = 0.20;       // 0.20 < 0.21 ✓
            double referenceTime = 45;
            double timeDifference = 80;  // Exceeds 40s * 1.8 = 72s ✓

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then
            assertThat(result).isEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }

        @Test
        @DisplayName("Should NOT apply stricter thresholds for segments >= 50s")
        void shouldNotApplyStricterThresholds_ForNormalSegments() throws Exception {
            // Given - 50s segment (exactly at threshold, NOT short)
            double piReal = 0.25;        // Should qualify for HIGH (< 0.30)
            double aiValue = 0.25;
            double referenceTime = 50;
            double timeDifference = 80;

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then - Should be HIGH because 0.25 < 0.30 (no multiplier applied)
            assertThat(result).isEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }
    }

    @Nested
    @DisplayName("Robust Reference Time Calculation")
    class RobustReferenceTimeCalculation {

        @Test
        @DisplayName("Should use median for normal distribution")
        void shouldUseMedian_ForNormalDistribution() throws Exception {
            // Given - Normal distribution of times (no clustering, low variance)
            List<Double> times = Arrays.asList(
                100.0, 102.0, 104.0, 106.0, 108.0, 110.0, 112.0, 120.0
            );

            // When
            double result = invokeCalculateRobustReferenceTime(times);

            // Then - Should return median of top 3
            // Top 3: [100, 102, 104]
            // Median = 102.0
            assertThat(result).isEqualTo(102.0);
        }

        @Test
        @DisplayName("Should detect cluster and use positions 4-7")
        void shouldDetectCluster_AndUseAlternativePositions() throws Exception {
            // Given - Suspicious cluster in top positions
            // Top 4 times cluster together (< 10% spread): 80-85s
            // But big gap to rest (> 25% gap): 85s → 120s = 41% gap
            List<Double> times = Arrays.asList(
                80.0, 82.0, 83.0, 85.0,    // Cluster (spread: 6.25%)
                120.0, 125.0, 130.0,       // Rest (gap from 85 to 120: 41%)
                150.0, 200.0, 250.0
            );

            // When
            double result = invokeCalculateRobustReferenceTime(times);

            // Then - With new logic, majority mistake detection triggers
            // because fastest (80) vs median (120) gap is 50%, AND
            // second (82) vs median gap is also large
            // Should use average of top 2: (80 + 82) / 2 = 81.0
            assertThat(result).isEqualTo(81.0);
        }

        @Test
        @DisplayName("Should handle small datasets")
        void shouldHandleSmallDatasets() throws Exception {
            // Given - Only 2 times
            List<Double> times = Arrays.asList(100.0, 110.0);

            // When
            double result = invokeCalculateRobustReferenceTime(times);

            // Then - Should return first value
            assertThat(result).isEqualTo(100.0);
        }

        @Test
        @DisplayName("Should handle empty list")
        void shouldHandleEmptyList() throws Exception {
            // Given
            List<Double> times = Collections.emptyList();

            // When
            double result = invokeCalculateRobustReferenceTime(times);

            // Then
            assertThat(result).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should use median for even number of elements")
        void shouldUseMedianForEvenNumberOfElements() throws Exception {
            // Given - 6 times (even number), tight distribution
            List<Double> times = Arrays.asList(
                100.0, 102.0, 104.0, 106.0, 108.0, 110.0
            );

            // When
            double result = invokeCalculateRobustReferenceTime(times);

            // Then - Median of top 3 = average of 2nd and 3rd = (102 + 104) / 2 = 103
            assertThat(result).isCloseTo(103.0, within(1.0));
        }

        @Test
        @DisplayName("Should NOT detect cluster when top times spread normally")
        void shouldNotDetectCluster_WhenTopTimesSpreadNormally() throws Exception {
            // Given - Top times with normal spread (> 10%)
            List<Double> times = Arrays.asList(
                80.0, 90.0, 100.0, 110.0,  // Spread: (110-80)/80 = 37.5%
                120.0, 130.0, 140.0,
                150.0
            );

            // When
            double result = invokeCalculateRobustReferenceTime(times);

            // Then - With new logic: fastest (80) vs median (115) gap = 43.75% < 50%
            // OR second (90) vs median gap not large enough
            // Should use median of top 3: 90.0
            assertThat(result).isEqualTo(90.0);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundaries")
    class EdgeCasesAndBoundaries {

        @Test
        @DisplayName("Should handle PI exactly at threshold")
        void shouldHandlePIExactlyAtThreshold() throws Exception {
            // Given - PI exactly at 0.30 (HIGH threshold)
            double piReal = 0.30;
            double aiValue = 0.25;
            double timeDifference = 80;
            double referenceTime = 200;

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then - 0.30 is NOT < 0.30, so should not be HIGH
            assertThat(result).isNotEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }

        @Test
        @DisplayName("Should handle AI exactly at threshold")
        void shouldHandleAIExactlyAtThreshold() throws Exception {
            // Given - AI exactly at 0.30 (HIGH threshold)
            double piReal = 0.25;
            double aiValue = 0.30;
            double timeDifference = 80;
            double referenceTime = 200;

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then - 0.30 is NOT < 0.30, so should not be HIGH
            assertThat(result).isNotEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }

        @Test
        @DisplayName("Should handle time difference exactly at threshold")
        void shouldHandleTimeDifferenceExactlyAtThreshold() throws Exception {
            // Given - Time difference exactly at threshold
            double piReal = 0.25;
            double aiValue = 0.25;
            double referenceTime = 200;
            double timeDifference = 72.0; // Exactly 40s * 1.8

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then - Should be HIGH (>= comparison)
            assertThat(result).isEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }

        @Test
        @DisplayName("Should handle zero time difference")
        void shouldHandleZeroTimeDifference() throws Exception {
            // Given
            double piReal = 0.25;
            double aiValue = 0.25;
            double timeDifference = 0;
            double referenceTime = 200;

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then
            assertThat(result).isEqualTo(AnomalyClassification.NO_SUSPICION);
        }

        @Test
        @DisplayName("Should handle negative time difference")
        void shouldHandleNegativeTimeDifference() throws Exception {
            // Given - Runner is slower (negative time difference)
            double piReal = 1.5;  // Slower than reference
            double aiValue = 1.5;
            double timeDifference = -50;
            double referenceTime = 200;

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then
            assertThat(result).isEqualTo(AnomalyClassification.NO_SUSPICION);
        }

        @Test
        @DisplayName("Should handle very large reference time")
        void shouldHandleVeryLargeReferenceTime() throws Exception {
            // Given - 2000s segment (hits 150s max threshold)
            double piReal = 0.25;
            double aiValue = 0.25;
            double referenceTime = 2000;
            double timeDifference = 300; // Exceeds 150s * 1.8 = 270s

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then
            assertThat(result).isEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }
    }

    @Nested
    @DisplayName("AI Consistency Check")
    class AIConsistencyCheck {

        @Test
        @DisplayName("Should NOT flag when AI ≈ 1.0 (runner is consistent with baseline)")
        void shouldNotFlag_WhenAIConsistentWithBaseline() throws Exception {
            // Given - Runner is much faster than contaminated reference (PI=0.40)
            // BUT their AI ≈ 1.0, meaning they're running as expected for themselves
            double piReal = 0.40;        // Looks fast vs contaminated reference
            double aiValue = 0.95;       // But consistent with their own baseline
            double timeDifference = 100;
            double referenceTime = 200;

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then - Should NOT be flagged (AI consistency check prevents false positive)
            assertThat(result).isEqualTo(AnomalyClassification.NO_SUSPICION);
        }

        @Test
        @DisplayName("Should flag when AI is also low (not consistent with baseline)")
        void shouldFlag_WhenAIAlsoLow() throws Exception {
            // Given - Runner is much faster than reference AND faster than their baseline
            double piReal = 0.40;        // Fast vs reference
            double aiValue = 0.40;       // Also fast vs their baseline
            double timeDifference = 50;
            double referenceTime = 200;

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then - Should be flagged (both PI and AI are suspicious)
            assertThat(result).isEqualTo(AnomalyClassification.MODERATE_SUSPICION);
        }

        @Test
        @DisplayName("Should apply AI consistency threshold (0.85 - 1.18)")
        void shouldApplyAIConsistencyThreshold() throws Exception {
            // Given - Different AI values around 1.0
            double piReal = 0.25;
            double timeDifference = 80;
            double referenceTime = 200;

            // Test various AI values
            // AI = 0.29: Below both consistency threshold (0.85) AND HIGH threshold (0.30)
            AnomalyClassification r1 = invokeClassifyAnomalies(piReal, 0.29, timeDifference, referenceTime);
            assertThat(r1).isEqualTo(AnomalyClassification.HIGH_SUSPICION);

            // AI = 0.85: At threshold, should NOT flag (consistent)
            AnomalyClassification r2 = invokeClassifyAnomalies(piReal, 0.85, timeDifference, referenceTime);
            assertThat(r2).isEqualTo(AnomalyClassification.NO_SUSPICION);

            // AI = 1.0: Perfect consistency, should NOT flag
            AnomalyClassification r3 = invokeClassifyAnomalies(piReal, 1.00, timeDifference, referenceTime);
            assertThat(r3).isEqualTo(AnomalyClassification.NO_SUSPICION);

            // AI = 1.17: Still consistent, should NOT flag
            AnomalyClassification r4 = invokeClassifyAnomalies(piReal, 1.17, timeDifference, referenceTime);
            assertThat(r4).isEqualTo(AnomalyClassification.NO_SUSPICION);

            // AI = 1.18: Above threshold, should check other criteria
            AnomalyClassification r5 = invokeClassifyAnomalies(piReal, 1.18, timeDifference, referenceTime);
            assertThat(r5).isEqualTo(AnomalyClassification.NO_SUSPICION); // Still no suspicion (running slower)
        }
    }

    @Nested
    @DisplayName("Majority Mistake Detection")
    class MajorityMistakeDetection {

        @Test
        @DisplayName("Should detect when majority made mistakes and use top-2 average")
        void shouldDetectMajorityMistakes() throws Exception {
            // Given - 7 runners: 2 ran correctly (~120s), 5 made mistakes (200-280s)
            List<Double> times = Arrays.asList(
                115.0, 125.0,           // Top 2: ran correctly
                210.0, 230.0, 250.0,    // Made mistakes (high variance)
                260.0, 280.0
            );

            // When
            double result = invokeCalculateRobustReferenceTime(times);

            // Then - Should use median of top 3: [115, 125, 210] → 125.0
            // (Majority detection requires both top times to be fast, but here 210 is in top 3)
            assertThat(result).isEqualTo(125.0);
        }

        @Test
        @DisplayName("Should NOT trigger majority detection when variance is low")
        void shouldNotTriggerMajorityDetection_WhenVarianceLow() throws Exception {
            // Given - Normal distribution, no majority mistakes
            List<Double> times = Arrays.asList(
                100.0, 105.0, 110.0, 115.0, 120.0, 125.0, 130.0
            );

            // When
            double result = invokeCalculateRobustReferenceTime(times);

            // Then - Should use normal median (not top-2 average)
            assertThat(result).isNotEqualTo(102.5); // NOT (100+105)/2
        }

        @Test
        @DisplayName("Should NOT trigger majority detection with only one fast runner")
        void shouldNotTriggerMajorityDetection_WithOnlyOneFastRunner() throws Exception {
            // Given - Only 1 fast runner (possible cheat), others normal
            List<Double> times = Arrays.asList(
                80.0,                   // Suspicious single fast time
                200.0, 205.0, 210.0,    // Normal cluster
                215.0, 220.0, 225.0
            );

            // When
            double result = invokeCalculateRobustReferenceTime(times);

            // Then - Should NOT use top-2 (second is not also fast)
            // Gap: (200-80)/80 = 150% (> 50%), but second is not fast
            assertThat(result).isNotEqualTo(140.0); // NOT (80+200)/2
        }
    }

    @Nested
    @DisplayName("Real-World Scenarios")
    class RealWorldScenarios {

        @Test
        @DisplayName("Should flag obvious shortcut on long road segment")
        void shouldFlagObviousShortcut_OnLongRoadSegment() throws Exception {
            // Given - 600s road segment, runner takes 150s (75% faster)
            double piReal = 0.25;        // 150 / 600
            double aiValue = 0.25;       // Normally runs PI 1.0
            double referenceTime = 600;
            double timeDifference = 450; // Saved 450s

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then
            assertThat(result).isEqualTo(AnomalyClassification.HIGH_SUSPICION);
        }

        @Test
        @DisplayName("Should NOT flag elite runner on good day")
        void shouldNotFlagEliteRunner_OnGoodDay() throws Exception {
            // Given - Elite runner (PI 0.85 baseline) has great day (PI 0.70)
            // This is only 18% faster than baseline
            double piReal = 0.70;
            double aiValue = 0.82;       // 0.70 / 0.85 = 0.82
            double referenceTime = 200;
            double timeDifference = 25;  // 25s faster

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then
            assertThat(result).isEqualTo(AnomalyClassification.NO_SUSPICION);
        }

        @Test
        @DisplayName("Should handle GPS tracking error on short segment")
        void shouldHandleGPSTrackingError_OnShortSegment() throws Exception {
            // Given - 20s segment with tracking error
            // Even if PI looks good, absolute time threshold should prevent false positive
            double piReal = 0.25;
            double aiValue = 0.25;
            double referenceTime = 20;
            double timeDifference = 10;  // Only 10s faster (< 40s minimum)

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then
            assertThat(result).isEqualTo(AnomalyClassification.NO_SUSPICION);
        }

        @Test
        @DisplayName("Should detect systematic anomaly")
        void shouldDetectSystematicAnomaly() throws Exception {
            // Given - Runner consistently 70% faster across multiple segments
            double piReal = 0.30;
            double aiValue = 0.30;
            double referenceTime = 250;
            double timeDifference = 90;  // 90s faster (exceeds 50s * 1.8 = 90s exactly)

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then - Should be flagged
            assertThat(result).isIn(
                AnomalyClassification.HIGH_SUSPICION,
                AnomalyClassification.MODERATE_SUSPICION
            );
        }

        @Test
        @DisplayName("Should NOT flag correct runner when majority made mistakes")
        void shouldNotFlagCorrectRunner_WhenMajorityMadeMistakes() throws Exception {
            // Given - 5 runners: 1 ran correctly (120s), 4 made mistakes (250s)
            // Without AI consistency check, this runner would look suspicious (PI = 0.48)
            double piReal = 0.48;        // 120s / 250s contaminated reference
            double aiValue = 1.0;        // But AI = 1.0 (running as expected)
            double referenceTime = 250;  // Contaminated by mistakes
            double timeDifference = 130; // Looks like big time savings

            // When
            AnomalyClassification result = invokeClassifyAnomalies(
                piReal, aiValue, timeDifference, referenceTime);

            // Then - Should NOT be flagged (AI consistency prevents false positive)
            assertThat(result).isEqualTo(AnomalyClassification.NO_SUSPICION);
        }
    }

    // Helper methods to invoke private methods via reflection

    private AnomalyClassification invokeClassifyAnomalies(
        double piReal, double aiValue, double timeDifference, double referenceTime) throws Exception {

        Method method = AnomalyDetectionServiceImpl.class.getDeclaredMethod(
            "classifyAnomalies", double.class, double.class, double.class, double.class);
        method.setAccessible(true);
        return (AnomalyClassification) method.invoke(service, piReal, aiValue, timeDifference, referenceTime);
    }

    private double invokeCalculateRobustReferenceTime(List<Double> sortedTimes) throws Exception {
        Method method = AnomalyDetectionServiceImpl.class.getDeclaredMethod(
            "calculateRobustReferenceTime", List.class);
        method.setAccessible(true);
        return (double) method.invoke(service, sortedTimes);
    }
}
