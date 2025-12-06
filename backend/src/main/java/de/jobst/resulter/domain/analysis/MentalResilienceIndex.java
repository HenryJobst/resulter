package de.jobst.resulter.domain.analysis;

import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Mental Resilience Index (MRI) - Measures how a runner mentally reacts after making a navigation mistake.
 *
 * <p>MRI = PI(reaction segment) - Normal PI</p>
 *
 * <p>Interpretation:</p>
 * <ul>
 *   <li>MRI &lt; -0.05: Panic reaction (runs significantly faster than normal, risks another error)</li>
 *   <li>-0.05 ≤ MRI ≤ +0.05: Ice-Man (maintains stable pace despite mistake)</li>
 *   <li>MRI > +0.05: Resignation (slows down significantly, loses time through uncertainty)</li>
 * </ul>
 */
@ValueObject
public record MentalResilienceIndex(Double value) implements Comparable<MentalResilienceIndex> {

    /**
     * Stability tolerance - MRI values within ±this range are considered stable (Ice-Man).
     */
    public static final double STABILITY_TOLERANCE = 0.05;

    /**
     * Creates an MRI from reaction PI and normal PI.
     *
     * @param reactionPI PI on the segment following the mistake
     * @param normalPI   Runner's normal PI (average excluding mistakes)
     * @return MentalResilienceIndex
     */
    public static MentalResilienceIndex of(PerformanceIndex reactionPI, PerformanceIndex normalPI) {
        return new MentalResilienceIndex(reactionPI.value() - normalPI.value());
    }

    /**
     * Classifies the mental reaction based on the MRI value.
     *
     * @return Mental classification (Panic/Ice-Man/Resigner)
     */
    public MentalClassification classify() {
        return classify(STABILITY_TOLERANCE);
    }

    /**
     * Classifies the mental reaction using a custom stability tolerance.
     *
     * @param stabilityTolerance Custom tolerance for Ice-Man classification
     * @return Mental classification
     */
    public MentalClassification classify(double stabilityTolerance) {
        if (value < -stabilityTolerance) {
            return MentalClassification.PANIC;
        } else if (value > stabilityTolerance) {
            return MentalClassification.RESIGNER;
        } else {
            return MentalClassification.ICE_MAN;
        }
    }

    /**
     * Checks if this MRI indicates a panic reaction (significantly faster than normal).
     *
     * @return true if MRI < -STABILITY_TOLERANCE
     */
    public boolean isPanic() {
        return value < -STABILITY_TOLERANCE;
    }

    /**
     * Checks if this MRI indicates resignation (significantly slower than normal).
     *
     * @return true if MRI > +STABILITY_TOLERANCE
     */
    public boolean isResignation() {
        return value > STABILITY_TOLERANCE;
    }

    /**
     * Checks if this MRI indicates stable mental state (Ice-Man).
     *
     * @return true if -STABILITY_TOLERANCE ≤ MRI ≤ +STABILITY_TOLERANCE
     */
    public boolean isStable() {
        return Math.abs(value) <= STABILITY_TOLERANCE;
    }

    @Override
    public int compareTo(MentalResilienceIndex other) {
        return Double.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return String.format("MRI(%.3f, %s)", value, classify());
    }
}
