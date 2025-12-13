package de.jobst.resulter.domain.analysis;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

/**
 * Aggregate statistics for hanging detection analysis across all runners in a result list.
 *
 * <p>Provides overview metrics about the extent and severity of hanging behavior detected.</p>
 */
@ValueObject
public record HangingStatistics(
        int totalRunners,
        int runnersWithHanging,
        int totalHangingSegments,
        int highHangingRunners,
        int moderateHangingRunners,
        @Nullable Double averageHangingIndex,
        @Nullable Double medianHangingIndex
) {

    /**
     * Gets the percentage of runners who show hanging behavior.
     *
     * @return Percentage (0-100) of runners with hanging, or 0.0 if no runners
     */
    public double getHangingRunnerPercentage() {
        if (totalRunners == 0) {
            return 0.0;
        }
        return (double) runnersWithHanging / totalRunners * 100.0;
    }

    /**
     * Checks if any hanging behavior was detected.
     *
     * @return true if at least one hanging segment was found
     */
    public boolean hasHanging() {
        return totalHangingSegments > 0;
    }

    @Override
    public String toString() {
        return String.format("HangingStatistics(totalRunners=%d, withHanging=%d (%.1f%%), " +
                             "totalSegments=%d, high=%d, moderate=%d, avgHI=%s, medianHI=%s)",
                totalRunners, runnersWithHanging, getHangingRunnerPercentage(),
                totalHangingSegments, highHangingRunners, moderateHangingRunners,
                averageHangingIndex != null ? String.format("%.3f", averageHangingIndex) : "N/A",
                medianHangingIndex != null ? String.format("%.3f", medianHangingIndex) : "N/A");
    }
}
