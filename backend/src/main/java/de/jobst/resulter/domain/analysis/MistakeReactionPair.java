package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.ControlCode;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Represents a mistake-reaction pair: a navigation mistake on one segment
 * and the mental/physical reaction on the following segment.
 *
 * <p>This captures:</p>
 * <ul>
 *   <li>Where the mistake occurred (mistake segment controls and PI)</li>
 *   <li>How the runner reacted mentally on the next segment (reaction PI and MRI)</li>
 *   <li>Classification of the reaction (Panic/Ice-Man/Resigner)</li>
 * </ul>
 */
@ValueObject
public record MistakeReactionPair(
        int mistakeLegNumber,
        ControlCode mistakeFromControl,
        ControlCode mistakeToControl,
        PerformanceIndex mistakePI,
        int reactionLegNumber,
        ControlCode reactionFromControl,
        ControlCode reactionToControl,
        PerformanceIndex reactionPI,
        MentalResilienceIndex mri,
        MentalClassification classification
) {
    /**
     * Creates a mistake-reaction pair.
     *
     * <p>Note: Mistake detection is done using Winsplits criteria (median + 25% and > 30s absolute),
     * not the simple PI >= 1.3 threshold. This allows detection of context-sensitive mistakes.</p>
     *
     * @param mistakeLegNumber     Leg number where mistake occurred (0-based)
     * @param mistakeFromControl   Control code where mistake segment starts
     * @param mistakeToControl     Control code where mistake segment ends
     * @param mistakePI            Performance Index on the mistake segment (detected by Winsplits criteria)
     * @param reactionLegNumber    Leg number of reaction segment (mistakeLegNumber + 1)
     * @param reactionFromControl  Control code where reaction segment starts
     * @param reactionToControl    Control code where reaction segment ends
     * @param reactionPI           Performance Index on the reaction segment
     * @param mri                  Mental Resilience Index (reactionPI - normalPI)
     * @param classification       Classification of the reaction
     * @throws IllegalArgumentException if reaction leg is not immediately after mistake leg
     */
    public MistakeReactionPair {
        if (reactionLegNumber != mistakeLegNumber + 1) {
            throw new IllegalArgumentException(
                    "Reaction leg must be immediately after mistake leg, expected: " + (mistakeLegNumber + 1) + ", was: " + reactionLegNumber
            );
        }
    }

    /**
     * Gets the mistake severity category.
     *
     * @return "moderate" (30-50% slower), "major" (50-100% slower), or "severe" (>100% slower)
     */
    public String getMistakeSeverity() {
        double pi = mistakePI.value();
        if (pi >= 2.0) {
            return "severe";    // 100% or more slower than best
        } else if (pi >= 1.50) {
            return "major";     // 50-100% slower
        } else {
            return "moderate";  // 30-50% slower
        }
    }

    /**
     * Checks if this represents a panic reaction.
     *
     * @return true if classification is PANIC
     */
    public boolean isPanicReaction() {
        return classification == MentalClassification.PANIC;
    }

    /**
     * Checks if this represents a stable (Ice-Man) reaction.
     *
     * @return true if classification is ICE_MAN
     */
    public boolean isStableReaction() {
        return classification == MentalClassification.ICE_MAN;
    }

    /**
     * Checks if this represents a resignation reaction.
     *
     * @return true if classification is RESIGNER
     */
    public boolean isResignationReaction() {
        return classification == MentalClassification.RESIGNER;
    }
}
