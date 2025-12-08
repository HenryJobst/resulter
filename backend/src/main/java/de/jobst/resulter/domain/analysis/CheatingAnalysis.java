package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.ResultListId;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.List;

/**
 * Complete cheating analysis for a result list.
 *
 * <p>Contains all runner cheating profiles.</p>
 *
 */
@ValueObject
public record CheatingAnalysis(
        ResultListId resultListId,
        EventId eventId,
        List<RunnerCheatingProfile> runnerProfiles
) {
    /**
     * Creates a mental resilience analysis.
     *
     * @param resultListId  Result list identifier
     * @param eventId       Event identifier
     * @param runnerProfiles List of runner cheating profiles (immutable copy will be created)
     */
    public CheatingAnalysis {
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
     * Gets runners with high suspicion.
     *
     * @return list of runners with high suspicion
     */
    public List<RunnerCheatingProfile> getHighSuspicionRunners() {
        return runnerProfiles.stream()
                .filter(profile -> profile.classification() == CheatingClassification.HIGH_SUSPICION)
                .toList();
    }

    /**
     * Gets runners with moderate suspicion.
     *
     * @return list of runners with moderate suspicion
     */
    public List<RunnerCheatingProfile> getModerateSuspicionRunners() {
        return runnerProfiles.stream()
            .filter(profile -> profile.classification() == CheatingClassification.MODERATE_SUSPICION)
            .toList();
    }

    /**
     * Gets runners sorted by anomalies index (ascending: high suspicion first, no suspicion last).
     *
     * @return sorted list of runner profiles
     */
    public List<RunnerCheatingProfile> getRunnersSortedByAnomaliesIndex() {
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

    @Override
    public String toString() {
        return String.format("MentalResilienceAnalysis(resultList=%s, runners=%d)",
                resultListId, getRunnerCount());
    }
}
