package de.jobst.resulter.domain.analysis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for Performance Index (PI) calculation and mistake detection.
 */
@DisplayName("Performance Index")
class PerformanceIndexTest {

    @Nested
    @DisplayName("Creation and Calculation")
    class CreationAndCalculation {

        @Test
        @DisplayName("Should create PI when runner matches best time")
        void shouldCreatePI_WhenRunnerMatchesBestTime() {
            // Given
            double runnerTime = 100.0;
            double bestTime = 100.0;

            // When
            PerformanceIndex pi = PerformanceIndex.of(runnerTime, bestTime);

            // Then
            assertThat(pi.value()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Should create PI when runner is slower than best")
        void shouldCreatePI_WhenRunnerIsSlower() {
            // Given
            double runnerTime = 130.0;
            double bestTime = 100.0;

            // When
            PerformanceIndex pi = PerformanceIndex.of(runnerTime, bestTime);

            // Then
            assertThat(pi.value()).isEqualTo(1.30);
        }

        @Test
        @DisplayName("Should create PI when runner is faster than current best (becomes new best)")
        void shouldCreatePI_WhenRunnerIsFaster() {
            // Given: This shouldn't happen in normal flow, but test the math
            double runnerTime = 90.0;
            double bestTime = 100.0;

            // When
            PerformanceIndex pi = PerformanceIndex.of(runnerTime, bestTime);

            // Then
            assertThat(pi.value()).isEqualTo(0.90);
        }

        @ParameterizedTest
        @CsvSource({
            "100.0, 100.0, 1.000",
            "130.0, 100.0, 1.300",
            "150.0, 100.0, 1.500",
            "200.0, 100.0, 2.000",
            "260.0, 200.0, 1.300",
            "105.5, 100.0, 1.055"
        })
        @DisplayName("Should calculate correct PI for various times")
        void shouldCalculateCorrectPI(double runnerTime, double bestTime, double expectedPI) {
            // When
            PerformanceIndex pi = PerformanceIndex.of(runnerTime, bestTime);

            // Then
            assertThat(pi.value()).isCloseTo(expectedPI, within(0.001));
        }

        @Test
        @DisplayName("Should throw exception when best time is null")
        void shouldThrowException_WhenBestTimeIsNull() {
            // Given
            double runnerTime = 100.0;
            Double bestTime = null;

            // When/Then
            assertThatThrownBy(() -> PerformanceIndex.of(runnerTime, bestTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Best time must be positive");
        }

        @Test
        @DisplayName("Should throw exception when best time is zero")
        void shouldThrowException_WhenBestTimeIsZero() {
            // Given
            double runnerTime = 100.0;
            double bestTime = 0.0;

            // When/Then
            assertThatThrownBy(() -> PerformanceIndex.of(runnerTime, bestTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Best time must be positive");
        }

        @Test
        @DisplayName("Should throw exception when best time is negative")
        void shouldThrowException_WhenBestTimeIsNegative() {
            // Given
            double runnerTime = 100.0;
            double bestTime = -10.0;

            // When/Then
            assertThatThrownBy(() -> PerformanceIndex.of(runnerTime, bestTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Best time must be positive");
        }

        @Test
        @DisplayName("Should throw exception when runner time is null")
        void shouldThrowException_WhenRunnerTimeIsNull() {
            // Given
            Double runnerTime = null;
            double bestTime = 100.0;

            // When/Then
            assertThatThrownBy(() -> PerformanceIndex.of(runnerTime, bestTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Runner time must be non-negative");
        }

        @Test
        @DisplayName("Should throw exception when runner time is negative")
        void shouldThrowException_WhenRunnerTimeIsNegative() {
            // Given
            double runnerTime = -10.0;
            double bestTime = 100.0;

            // When/Then
            assertThatThrownBy(() -> PerformanceIndex.of(runnerTime, bestTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Runner time must be non-negative");
        }
    }

    @Nested
    @DisplayName("Mistake Detection")
    class MistakeDetection {

        @Test
        @DisplayName("Should NOT be a mistake when PI equals 1.0 (best time)")
        void shouldNotBeMistake_WhenPIIsOne() {
            // Given
            PerformanceIndex pi = new PerformanceIndex(1.0);

            // When/Then
            assertThat(pi.isMistake()).isFalse();
        }

        @Test
        @DisplayName("Should NOT be a mistake when PI is below threshold")
        void shouldNotBeMistake_WhenPIIsBelowThreshold() {
            // Given
            PerformanceIndex pi = new PerformanceIndex(1.29);

            // When/Then
            assertThat(pi.isMistake()).isFalse();
        }

        @Test
        @DisplayName("Should BE a mistake when PI equals threshold (1.30)")
        void shouldBeMistake_WhenPIEqualsThreshold() {
            // Given
            PerformanceIndex pi = new PerformanceIndex(1.30);

            // When/Then
            assertThat(pi.isMistake()).isTrue();
        }

        @Test
        @DisplayName("Should BE a mistake when PI exceeds threshold")
        void shouldBeMistake_WhenPIExceedsThreshold() {
            // Given
            PerformanceIndex pi = new PerformanceIndex(1.50);

            // When/Then
            assertThat(pi.isMistake()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(doubles = {1.30, 1.31, 1.50, 2.00, 3.00})
        @DisplayName("Should BE a mistake when PI >= 1.30")
        void shouldBeMistake_WhenPIIsAtOrAboveThreshold(double piValue) {
            // Given
            PerformanceIndex pi = new PerformanceIndex(piValue);

            // When/Then
            assertThat(pi.isMistake()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(doubles = {1.0, 1.1, 1.2, 1.29, 1.299})
        @DisplayName("Should NOT be a mistake when PI < 1.30")
        void shouldNotBeMistake_WhenPIIsBelowThreshold(double piValue) {
            // Given
            PerformanceIndex pi = new PerformanceIndex(piValue);

            // When/Then
            assertThat(pi.isMistake()).isFalse();
        }

        @Test
        @DisplayName("Should use custom threshold when provided")
        void shouldUseCustomThreshold() {
            // Given
            PerformanceIndex pi = new PerformanceIndex(1.40);
            double customThreshold = 1.50;

            // When/Then
            assertThat(pi.isMistake(customThreshold)).isFalse();
            assertThat(pi.isMistake()).isTrue(); // Default threshold
        }
    }

    @Nested
    @DisplayName("Comparison and Ordering")
    class ComparisonAndOrdering {

        @Test
        @DisplayName("Should compare PIs correctly")
        void shouldComparePIsCorrectly() {
            // Given
            PerformanceIndex pi1 = new PerformanceIndex(1.0);
            PerformanceIndex pi2 = new PerformanceIndex(1.30);
            PerformanceIndex pi3 = new PerformanceIndex(1.50);

            // When/Then
            assertThat(pi1).isLessThan(pi2);
            assertThat(pi2).isLessThan(pi3);
            assertThat(pi1).isLessThan(pi3);
        }

        @Test
        @DisplayName("Should consider equal PIs as equal")
        void shouldConsiderEqualPIsAsEqual() {
            // Given
            PerformanceIndex pi1 = new PerformanceIndex(1.30);
            PerformanceIndex pi2 = new PerformanceIndex(1.30);

            // When/Then
            assertThat(pi1).isEqualByComparingTo(pi2);
        }
    }

    @Nested
    @DisplayName("String Representation")
    class StringRepresentation {

        @Test
        @DisplayName("Should format toString with 3 decimal places")
        void shouldFormatToStringCorrectly() {
            // Given
            PerformanceIndex pi = new PerformanceIndex(1.305);

            // When
            String result = pi.toString();

            // Then
            assertThat(result).startsWith("PI(1");
            assertThat(result).contains("305");
            assertThat(result).endsWith(")");
        }
    }

    @Nested
    @DisplayName("Boundary Conditions")
    class BoundaryConditions {

        @Test
        @DisplayName("Should handle very small times")
        void shouldHandleVerySmallTimes() {
            // Given
            double runnerTime = 0.001;
            double bestTime = 0.001;

            // When
            PerformanceIndex pi = PerformanceIndex.of(runnerTime, bestTime);

            // Then
            assertThat(pi.value()).isCloseTo(1.0, within(0.001));
        }

        @Test
        @DisplayName("Should handle very large times")
        void shouldHandleVeryLargeTimes() {
            // Given
            double runnerTime = 10000.0;
            double bestTime = 5000.0;

            // When
            PerformanceIndex pi = PerformanceIndex.of(runnerTime, bestTime);

            // Then
            assertThat(pi.value()).isCloseTo(2.0, within(0.001));
        }

        @Test
        @DisplayName("Should handle PI exactly at mistake threshold")
        void shouldHandlePIExactlyAtThreshold() {
            // Given
            double runnerTime = 130.0;
            double bestTime = 100.0;

            // When
            PerformanceIndex pi = PerformanceIndex.of(runnerTime, bestTime);

            // Then
            assertThat(pi.value()).isEqualTo(PerformanceIndex.MISTAKE_THRESHOLD);
            assertThat(pi.isMistake()).isTrue();
        }
    }
}
