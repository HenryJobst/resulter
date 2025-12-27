package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.RaceNumber;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.List;

/**
 * Complete mental resilience profile for a single runner.
 *
 * <p>Contains:</p>
 * <ul>
 *   <li>Runner identification (person ID, class, race number)</li>
 *   <li>Class statistics (runner count, reliability indicator)</li>
 *   <li>Normal performance baseline (Normal PI)</li>
 *   <li>All mistake-reaction pairs detected for this runner</li>
 *   <li>Average MRI across all mistakes</li>
 *   <li>Overall mental classification</li>
 * </ul>
 */
@ValueObject
public record RunnerMentalProfile(
        PersonId personId,
        String classResultShortName,
        RaceNumber raceNumber,
        int classRunnerCount,
        boolean reliableData,
        PerformanceIndex normalPI,
        List<MistakeReactionPair> mistakeReactions,
        Double averageMRI,
        MentalClassification classification
) implements Comparable<RunnerMentalProfile> {

    /**
     * Creates a runner mental profile.
     *
     * @param personId              Person identifier
     * @param classResultShortName  Class short name
     * @param raceNumber            Race number
     * @param classRunnerCount      Number of runners in this class
     * @param reliableData          True if the class has enough runners (≥5) for reliable analysis
     * @param normalPI              Normal Performance Index (baseline)
     * @param mistakeReactions      List of mistake-reaction pairs (immutable copy will be created)
     * @param averageMRI            Average MRI across all mistake reactions
     * @param classification        Overall mental classification based on average MRI
     */
    public RunnerMentalProfile {
        // Create immutable copy of list
        mistakeReactions = List.copyOf(mistakeReactions);
    }

    /**
     * Gets the number of mistakes this runner made.
     *
     * @return mistake count
     */
    public int getMistakeCount() {
        return mistakeReactions.size();
    }

    /**
     * Gets the number of panic reactions.
     *
     * @return panic reaction count
     */
    public int getPanicReactionCount() {
        return (int) mistakeReactions.stream()
                .filter(MistakeReactionPair::isPanicReaction)
                .count();
    }

    /**
     * Gets the number of stable (Ice-Man) reactions.
     *
     * @return stable reaction count
     */
    public int getStableReactionCount() {
        return (int) mistakeReactions.stream()
                .filter(MistakeReactionPair::isStableReaction)
                .count();
    }

    /**
     * Gets the number of resignation reactions.
     *
     * @return resignation reaction count
     */
    public int getResignationReactionCount() {
        return (int) mistakeReactions.stream()
                .filter(MistakeReactionPair::isResignationReaction)
                .count();
    }

    /**
     * Checks if this runner had any mistakes.
     *
     * @return true if at least one mistake was made
     */
    public boolean hasMistakes() {
        return !mistakeReactions.isEmpty();
    }

    /**
     * Compares runners by average MRI (ascending order: panic runners first, resigners last).
     *
     * @param other other runner profile
     * @return comparison result
     */
    @Override
    public int compareTo(RunnerMentalProfile other) {
        // Sort by average MRI: negative (panic) first, positive (resigner) last
        return Double.compare(this.averageMRI, other.averageMRI);
    }

    @Override
    public String toString() {
        return "RunnerProfile(personId=%s, class=%s, normalPI=%.3f, mistakes=%d, avgMRI=%.3f, %s)".formatted(
                personId, classResultShortName, normalPI.value(), getMistakeCount(), averageMRI, classification);
    }
}
