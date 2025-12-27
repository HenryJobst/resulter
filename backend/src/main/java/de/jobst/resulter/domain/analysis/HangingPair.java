package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.ControlCode;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.RaceNumber;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Represents a single hanging instance where a runner (passenger) followed a faster runner (bus driver)
 * on a specific segment and achieved better performance than their normal baseline.
 *
 * <p>A hanging pair is created when all three criteria are met:</p>
 * <ol>
 *   <li>Temporal proximity: Passenger punched within 30s after bus driver</li>
 *   <li>Performance hierarchy: Bus driver was faster on this segment</li>
 *   <li>Performance improvement: Passenger performed significantly better than their normal PI</li>
 * </ol>
 */
@ValueObject
public record HangingPair(
        int legNumber,
        ControlCode fromControl,
        ControlCode toControl,
        PersonId busDriverId,
        String busDriverClassName,
        RaceNumber busDriverRaceNumber,
        double timeDeltaSeconds,
        PerformanceIndex passengerSegmentPI,
        PerformanceIndex busDriverSegmentPI,
        HangingIndex hangingIndex,
        double passengerActualTime,
        double busDriverActualTime,
        double referenceTime
) {

    /**
     * Maximum allowed time delta between bus driver and passenger punch times (30 seconds).
     */
    public static final double MAX_TIME_DELTA_SECONDS = 30.0;

    /**
     * Compact canonical constructor with validation.
     */
    public HangingPair {
        if (legNumber < 1) {
            throw new IllegalArgumentException("Leg number must be positive, was: " + legNumber);
        }
        if (fromControl == null) {
            throw new IllegalArgumentException("From control cannot be null");
        }
        if (toControl == null) {
            throw new IllegalArgumentException("To control cannot be null");
        }
        if (busDriverId == null) {
            throw new IllegalArgumentException("Bus driver ID cannot be null");
        }
        if (busDriverClassName == null || busDriverClassName.isBlank()) {
            throw new IllegalArgumentException("Bus driver class name cannot be null or blank");
        }
        if (busDriverRaceNumber == null) {
            throw new IllegalArgumentException("Bus driver race number cannot be null");
        }
        if (timeDeltaSeconds < 0 || timeDeltaSeconds > MAX_TIME_DELTA_SECONDS) {
            throw new IllegalArgumentException(
                    "Time delta must be between 0 and %.1f seconds, was: %.2f".formatted(
                            MAX_TIME_DELTA_SECONDS, timeDeltaSeconds));
        }
        if (passengerSegmentPI == null) {
            throw new IllegalArgumentException("Passenger segment PI cannot be null");
        }
        if (busDriverSegmentPI == null) {
            throw new IllegalArgumentException("Bus driver segment PI cannot be null");
        }
        if (hangingIndex == null) {
            throw new IllegalArgumentException("Hanging index cannot be null");
        }
        if (passengerActualTime < 0) {
            throw new IllegalArgumentException("Passenger actual time cannot be negative, was: " + passengerActualTime);
        }
        if (busDriverActualTime < 0) {
            throw new IllegalArgumentException("Bus driver actual time cannot be negative, was: " + busDriverActualTime);
        }
        if (referenceTime <= 0) {
            throw new IllegalArgumentException("Reference time must be positive, was: " + referenceTime);
        }
    }

    /**
     * Gets the improvement percentage of the passenger compared to their normal performance.
     *
     * @return Improvement percentage (positive values mean better than expected)
     */
    public double getImprovementPercent() {
        return hangingIndex.getImprovementPercent();
    }

    /**
     * Checks if the bus driver was significantly faster than the passenger on this segment.
     *
     * @return true if bus driver's PI is lower than passenger's PI
     */
    public boolean isBusDriverFaster() {
        return busDriverSegmentPI.value() < passengerSegmentPI.value();
    }

    /**
     * Gets the time difference between bus driver and passenger on this segment.
     *
     * @return Time difference in seconds (positive means passenger was slower)
     */
    public double getTimeDifference() {
        return passengerActualTime - busDriverActualTime;
    }
}
