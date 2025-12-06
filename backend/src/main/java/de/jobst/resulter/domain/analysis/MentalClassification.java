package de.jobst.resulter.domain.analysis;

import lombok.Getter;

/**
 * Classification of mental reaction after a navigation mistake.
 *
 * <p>Based on Mental Resilience Index (MRI) values or error patterns:</p>
 * <ul>
 *   <li><strong>PANIC:</strong> MRI &lt; -0.05 - Runner runs significantly faster than normal after a mistake,
 *   risking another error due to overpacing and reduced concentration</li>
 *   <li><strong>ICE_MAN:</strong> -0.05 ≤ MRI ≤ +0.05 - Runner maintains consistent pace despite the mistake,
 *   showing mental stability and resilience</li>
 *   <li><strong>RESIGNER:</strong> MRI > +0.05 - Runner slows down significantly after a mistake,
 *   losing time through uncertainty and reduced confidence</li>
 *   <li><strong>CHAIN_ERROR:</strong> Reaction segment is also a mistake (PI > 1.30) - Runner makes
 *   consecutive errors, indicating loss of control or disorientation</li>
 * </ul>
 */
@Getter
public enum MentalClassification {
    /**
     * Panic reaction - Running too fast after mistake (MRI < -0.05).
     * <p>Risks: Another mistake due to overpacing, reduced concentration, poor route choice decisions</p>
     */
    PANIC("panic", -0.05),

    /**
     * Ice-Man - Maintains stable pace after mistake (-0.05 ≤ MRI ≤ +0.05).
     * <p>Optimal reaction: Shows mental resilience, ability to recover quickly, good race management</p>
     */
    ICE_MAN("ice_man", 0.0),

    /**
     * Resignation - Slowing down significantly after mistake (MRI > +0.05).
     * <p>Loses time: Due to uncertainty, reduced confidence, overly cautious navigation</p>
     */
    RESIGNER("resigner", 0.05),

    /**
     * Chain Error - Reaction segment is also a mistake (PI > 1.30).
     * <p>Consecutive errors: Indicates loss of control, disorientation, or inability to recover from the initial mistake</p>
     */
    CHAIN_ERROR("chain_error", Double.NaN);

    private final String key;
    private final double thresholdIndicator;

    MentalClassification(String key, double thresholdIndicator) {
        this.key = key;
        this.thresholdIndicator = thresholdIndicator;
    }

    /**
     * Returns a human-readable description of this classification.
     *
     * @return description
     */
    public String getDescription() {
        return switch (this) {
            case PANIC ->
                    "Runs significantly faster than normal after a mistake (MRI < -0.05), risking another error";
            case ICE_MAN ->
                    "Maintains normal pace despite mistakes (-0.05 ≤ MRI ≤ +0.05), showing mental stability";
            case RESIGNER ->
                    "Slows down significantly after a mistake (MRI > +0.05), losing time through uncertainty";
            case CHAIN_ERROR ->
                    "Makes consecutive mistakes (reaction segment PI > 1.30), indicating loss of control or disorientation";
        };
    }

    @Override
    public String toString() {
        return key;
    }
}
