package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.PunchTime;
import de.jobst.resulter.domain.RaceNumber;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.List;

/**
 * Hanging profile for a single runner.
 *
 * <p>Contains:</p>
 * <ul>
 *   <li>Runner identification (person ID, class, race number)</li>
 *   <li>Class statistics (runner count, reliability indicator)</li>
 *   <li>Normal performance baseline (Normal PI)</li>
 *   <li>List of detected hanging pairs (bus driver + passenger instances)</li>
 *   <li>Hanging classification and statistics</li>
 * </ul>
 */
@ValueObject
public record RunnerHangingProfile(
        PersonId personId,
        String classResultShortName,
        RaceNumber raceNumber,
        PunchTime startTime,
        int classRunnerCount,
        boolean reliableData,
        PerformanceIndex normalPI,
        List<HangingPair> hangingPairs,
        double averageHangingIndex,
        HangingClassification classification,
        int totalNonMistakeSegments
) implements Comparable<RunnerHangingProfile> {

    /**
     * Creates a runner hanging profile with defensive copy of hanging pairs list.
     */
    public RunnerHangingProfile {
        hangingPairs = List.copyOf(hangingPairs);
    }

    /**
     * Gets the number of unique hanging segments detected for this runner.
     * Multiple bus drivers on the same segment are counted as one hanging segment.
     *
     * @return Count of unique hanging segments
     */
    public int getHangingCount() {
        return (int) hangingPairs.stream()
                .map(pair -> new SegmentKey(pair.legNumber(), pair.fromControl().value(), pair.toControl().value()))
                .distinct()
                .count();
    }

    /**
     * Gets the percentage of segments that show hanging behavior.
     *
     * @return Percentage (0-100) of hanging segments out of total non-mistake segments
     */
    public double getHangingPercentage() {
        if (totalNonMistakeSegments == 0) {
            return 0.0;
        }
        int uniqueHangingSegments = getHangingCount();
        return (double) uniqueHangingSegments / totalNonMistakeSegments * 100.0;
    }

    /**
     * Helper record to identify unique segments for counting purposes.
     */
    private record SegmentKey(int legNumber, String fromControl, String toControl) {}

    /**
     * Checks if this runner has any hanging behavior detected.
     *
     * @return true if at least one hanging pair exists
     */
    public boolean hasHanging() {
        return !hangingPairs.isEmpty();
    }

    /**
     * Gets the minimum (best) hanging index value for this runner.
     * Lower values indicate more improvement.
     *
     * @return Minimum HI value, or null if no hanging pairs
     */
    public Double getMinimumHangingIndex() {
        return hangingPairs.stream()
                .map(HangingPair::hangingIndex)
                .map(HangingIndex::value)
                .min(Double::compare)
                .orElse(null);
    }

    /**
     * Gets the maximum improvement percentage achieved on any segment.
     *
     * @return Maximum improvement percentage, or 0.0 if no hanging pairs
     */
    public double getMaximumImprovement() {
        return hangingPairs.stream()
                .mapToDouble(HangingPair::getImprovementPercent)
                .max()
                .orElse(0.0);
    }

    /**
     * Compares runners by average hanging index (ascending order: most hanging first).
     *
     * @param other other runner profile
     * @return comparison result
     */
    @Override
    public int compareTo(RunnerHangingProfile other) {
        // Sort by average hanging index: lower values (more improvement) first
        return Double.compare(this.averageHangingIndex, other.averageHangingIndex);
    }

    @Override
    public String toString() {
        return String.format("RunnerHangingProfile(personId=%s, class=%s, normalPI=%.3f, " +
                             "hangingPairs=%d, avgHI=%.3f, %s)",
                personId, classResultShortName, normalPI.value(),
                hangingPairs.size(), averageHangingIndex, classification);
    }
}
