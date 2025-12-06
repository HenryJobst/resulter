package de.jobst.resulter.domain.analysis;

import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

/**
 * Aggregate statistics for Mental Resilience Index analysis across all runners in a result list.
 *
 * <p>Provides overview metrics including:</p>
 * <ul>
 *   <li>Total runner count and runners with mistakes</li>
 *   <li>Distribution across mental classifications (Panic/Ice-Man/Resigner)</li>
 *   <li>Average and median MRI values</li>
 * </ul>
 */
@ValueObject
public record MriStatistics(
        int totalRunners,
        int runnersWithMistakes,
        int totalMistakes,
        int panicReactions,
        int iceManReactions,
        int resignerReactions,
        @Nullable Double averageMRI,
        @Nullable Double medianMRI
) {
    /**
     * Creates MRI statistics.
     *
     * @param totalRunners        Total number of runners analyzed
     * @param runnersWithMistakes Number of runners who made at least one mistake
     * @param totalMistakes       Total number of mistakes detected across all runners
     * @param panicReactions      Number of panic reactions (MRI < -0.05)
     * @param iceManReactions     Number of stable reactions (-0.05 ≤ MRI ≤ +0.05)
     * @param resignerReactions   Number of resignation reactions (MRI > +0.05)
     * @param averageMRI          Average MRI across all mistake-reaction pairs
     * @param medianMRI           Median MRI across all mistake-reaction pairs
     */
    public MriStatistics {
        if (totalRunners < 0) {
            throw new IllegalArgumentException("Total runners cannot be negative");
        }
        if (runnersWithMistakes < 0 || runnersWithMistakes > totalRunners) {
            throw new IllegalArgumentException("Runners with mistakes must be between 0 and total runners");
        }
        if (totalMistakes < 0) {
            throw new IllegalArgumentException("Total mistakes cannot be negative");
        }
        if (panicReactions < 0 || iceManReactions < 0 || resignerReactions < 0) {
            throw new IllegalArgumentException("Reaction counts cannot be negative");
        }
    }

    /**
     * Gets the percentage of runners who made mistakes.
     *
     * @return percentage (0.0 to 100.0), or 0.0 if no runners
     */
    public double getPercentageWithMistakes() {
        return totalRunners > 0 ? (double) runnersWithMistakes / totalRunners * 100.0 : 0.0;
    }

    /**
     * Gets the percentage of panic reactions among all reactions.
     *
     * @return percentage (0.0 to 100.0), or 0.0 if no reactions
     */
    public double getPercentagePanic() {
        int totalReactions = panicReactions + iceManReactions + resignerReactions;
        return totalReactions > 0 ? (double) panicReactions / totalReactions * 100.0 : 0.0;
    }

    /**
     * Gets the percentage of stable (Ice-Man) reactions among all reactions.
     *
     * @return percentage (0.0 to 100.0), or 0.0 if no reactions
     */
    public double getPercentageIceMan() {
        int totalReactions = panicReactions + iceManReactions + resignerReactions;
        return totalReactions > 0 ? (double) iceManReactions / totalReactions * 100.0 : 0.0;
    }

    /**
     * Gets the percentage of resignation reactions among all reactions.
     *
     * @return percentage (0.0 to 100.0), or 0.0 if no reactions
     */
    public double getPercentageResigner() {
        int totalReactions = panicReactions + iceManReactions + resignerReactions;
        return totalReactions > 0 ? (double) resignerReactions / totalReactions * 100.0 : 0.0;
    }

    /**
     * Gets the total number of reactions (should equal total mistakes if all have reaction segments).
     *
     * @return total reactions
     */
    public int getTotalReactions() {
        return panicReactions + iceManReactions + resignerReactions;
    }

    /**
     * Checks if there are any mistakes in the analysis.
     *
     * @return true if at least one mistake was detected
     */
    public boolean hasMistakes() {
        return totalMistakes > 0;
    }
}
