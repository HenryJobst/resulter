package de.jobst.resulter.domain.analysis;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

/**
 * Hanging Index (HI) - Measures performance improvement when a runner follows another runner.
 *
 * <p>HI = Segment PI / Expected PI (Normal PI)</p>
 *
 * <p>Interpretation:</p>
 * <ul>
 *   <li>HI = 1.0: Runner performed as expected (normal performance)</li>
 *   <li>HI &lt; 1.0: Runner performed better than expected (possible hanging)</li>
 *   <li>HI ≤ 0.85: Indicates hanging behavior (at least 15% improvement)</li>
 *   <li>HI > 1.0: Runner performed worse than expected (not hanging)</li>
 * </ul>
 *
 * <p>The Hanging Index quantifies the anomaly when a runner (passenger) achieves significantly
 * better performance than their baseline by following a faster runner (bus driver).</p>
 */
@ValueObject
public record HangingIndex(Double value) implements Comparable<HangingIndex> {

    /**
     * Hanging threshold - HI values at or below this indicate hanging behavior.
     * 0.85 = at least 15% improvement over normal performance.
     */
    public static final double HANGING_THRESHOLD = 0.85;

    /**
     * Creates a HangingIndex from segment PI and expected PI (Normal PI).
     *
     * @param segmentPI  Segment Performance Index
     * @param expectedPI Expected Performance Index (runner's Normal PI)
     * @return HangingIndex
     * @throws IllegalArgumentException if expectedPI is null or <= 0
     */
    public static HangingIndex of(PerformanceIndex segmentPI, @Nullable PerformanceIndex expectedPI) {
        if (expectedPI == null || expectedPI.value() == null || expectedPI.value() <= 0) {
            throw new IllegalArgumentException("Expected PI must be positive, was: " + expectedPI);
        }
        if (segmentPI == null || segmentPI.value() == null || segmentPI.value() < 0) {
            throw new IllegalArgumentException("Segment PI must be non-negative, was: " + segmentPI);
        }
        return new HangingIndex(segmentPI.value() / expectedPI.value());
    }

    /**
     * Checks if this hanging index indicates hanging behavior.
     *
     * @return true if HI <= HANGING_THRESHOLD (at least 15% improvement)
     */
    public boolean isHanging() {
        return value <= HANGING_THRESHOLD;
    }

    /**
     * Checks if this hanging index indicates hanging behavior using a custom threshold.
     *
     * @param threshold Custom hanging threshold
     * @return true if HI <= threshold
     */
    public boolean isHanging(double threshold) {
        return value <= threshold;
    }

    /**
     * Calculates the improvement percentage.
     * For example, HI = 0.75 means 25% improvement (25% better than expected).
     *
     * @return Improvement percentage (positive values mean better than expected)
     */
    public double getImprovementPercent() {
        return (1.0 - value) * 100.0;
    }

    /**
     * Calculates the degradation percentage for cases where performance is worse than expected.
     * For example, HI = 1.20 means 20% worse than expected.
     *
     * @return Degradation percentage (positive values mean worse than expected), or 0 if improved
     */
    public double getDegradationPercent() {
        if (value <= 1.0) {
            return 0.0;
        }
        return (value - 1.0) * 100.0;
    }

    @Override
    public int compareTo(HangingIndex other) {
        // Lower HI values indicate more hanging (more improvement)
        // So we want ascending order
        return Double.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return "HI(%.3f)".formatted(value);
    }
}
