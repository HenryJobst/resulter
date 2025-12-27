package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.ResultListId;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.List;

/**
 * Complete Mental Resilience Index analysis for a result list.
 *
 * <p>Contains all runner mental profiles and aggregate statistics for analyzing
 * how runners mentally react after making navigation mistakes.</p>
 *
 * <p>The analysis is performed at the result list level, but is designed to support
 * future aggregations at cup and year levels.</p>
 */
@ValueObject
public record MentalResilienceAnalysis(
        ResultListId resultListId,
        EventId eventId,
        List<RunnerMentalProfile> runnerProfiles,
        MriStatistics statistics
) {
    /**
     * Creates a mental resilience analysis.
     *
     * @param resultListId  Result list identifier
     * @param eventId       Event identifier
     * @param runnerProfiles List of runner mental profiles (immutable copy will be created)
     * @param statistics    Aggregate statistics
     */
    public MentalResilienceAnalysis {
        // Create immutable copy of list
        runnerProfiles = List.copyOf(runnerProfiles);
    }

    /**
     * Gets the number of runners analyzed.
     *
     * @return runner count
     */
    public int getRunnerCount() {
        return runnerProfiles.size();
    }

    /**
     * Gets runners with panic reactions (average MRI < -0.05).
     *
     * @return list of panic runners
     */
    public List<RunnerMentalProfile> getPanicRunners() {
        return runnerProfiles.stream()
                .filter(profile -> profile.classification() == MentalClassification.PANIC)
                .toList();
    }

    /**
     * Gets runners with stable (Ice-Man) reactions (-0.05 ≤ average MRI ≤ +0.05).
     *
     * @return list of stable runners
     */
    public List<RunnerMentalProfile> getIceManRunners() {
        return runnerProfiles.stream()
                .filter(profile -> profile.classification() == MentalClassification.ICE_MAN)
                .toList();
    }

    /**
     * Gets runners with resignation reactions (average MRI > +0.05).
     *
     * @return list of runners with resignation tendency
     */
    public List<RunnerMentalProfile> getResignerRunners() {
        return runnerProfiles.stream()
                .filter(profile -> profile.classification() == MentalClassification.RESIGNER)
                .toList();
    }

    /**
     * Gets runners sorted by average MRI (ascending: panic first, resignation last).
     *
     * @return sorted list of runner profiles
     */
    public List<RunnerMentalProfile> getRunnersSortedByMRI() {
        return runnerProfiles.stream()
                .sorted()
                .toList();
    }

    /**
     * Checks if the analysis contains any data.
     *
     * @return true if at least one runner profile exists
     */
    public boolean hasData() {
        return !runnerProfiles.isEmpty();
    }

    /**
     * Checks if the analysis contains any mistakes.
     *
     * @return true if at least one mistake was detected
     */
    public boolean hasMistakes() {
        return statistics.hasMistakes();
    }

    @Override
    public String toString() {
        return "MentalResilienceAnalysis(resultList=%s, runners=%d, mistakes=%d, avgMRI=%.3f)".formatted(
                resultListId, getRunnerCount(), statistics.totalMistakes(), statistics.averageMRI());
    }
}
