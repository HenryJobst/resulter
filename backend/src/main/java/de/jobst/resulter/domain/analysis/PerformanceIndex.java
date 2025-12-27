package de.jobst.resulter.domain.analysis;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

/**
 * Performance Index (PI) - Normalizes runner performance relative to the best time on a segment.
 *
 * <p>PI = Runner's time / Best time on segment</p>
 *
 * <p>Interpretation:</p>
 * <ul>
 *   <li>PI = 1.0: Runner matched the best time (perfect performance)</li>
 *   <li>PI &lt; 1.0: Not possible (runner IS the best)</li>
 *   <li>PI > 1.0: Runner was slower than best (e.g., 1.30 = 30% slower)</li>
 *   <li>PI > 1.30: Considered a mistake (30% slower than best)</li>
 * </ul>
 */
@ValueObject
public record PerformanceIndex(@Nullable Double value) implements Comparable<PerformanceIndex> {

    /**
     * Mistake threshold - PI values above this are considered navigation mistakes.
     */
    public static final double MISTAKE_THRESHOLD = 1.30;

    /**
     * Creates a PerformanceIndex from runner time and best time.
     *
     * @param runnerTime Runner's time on the segment in seconds
     * @param bestTime   Best time on the segment in seconds
     * @return PerformanceIndex
     * @throws IllegalArgumentException if bestTime is null or <= 0
     */
    public static PerformanceIndex of(@Nullable Double runnerTime, @Nullable Double bestTime) {
        if (bestTime == null || bestTime <= 0) {
            throw new IllegalArgumentException("Best time must be positive, was: " + bestTime);
        }
        if (runnerTime == null || runnerTime < 0) {
            throw new IllegalArgumentException("Runner time must be non-negative, was: " + runnerTime);
        }
        return new PerformanceIndex(runnerTime / bestTime);
    }

    /**
     * Checks if this performance index indicates a mistake.
     *
     * @return true if PI >= MISTAKE_THRESHOLD (30% slower than best)
     */
    public boolean isMistake() {
        return value != null && value >= MISTAKE_THRESHOLD;
    }

    /**
     * Checks if this performance index indicates a mistake using a custom threshold.
     *
     * @param threshold Custom mistake threshold
     * @return true if PI >= threshold
     */
    public boolean isMistake(double threshold) {
        return value != null && value >= threshold;
    }

    @Override
    public int compareTo(PerformanceIndex other) {
        return Double.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return "PI(%.3f)".formatted(value);
    }
}
