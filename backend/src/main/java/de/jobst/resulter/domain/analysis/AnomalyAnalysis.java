package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.ResultListId;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.List;

/**
 * Complete anomaly analysis for a result list.
 *
 * <p>Contains all runner anomaly profiles.</p>
 *
 */
@ValueObject
public record AnomalyAnalysis(
        ResultListId resultListId,
        EventId eventId,
        List<RunnerAnomalyProfile> runnerProfiles
) {
    /**
     * Creates a mental resilience analysis.
     *
     * @param resultListId  Result list identifier
     * @param eventId       Event identifier
     * @param runnerProfiles List of runner anomaly profiles (immutable copy will be created)
     */
    public AnomalyAnalysis {
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
    public List<RunnerAnomalyProfile> getHighSuspicionRunners() {
        return runnerProfiles.stream()
                .filter(profile -> profile.classification() == AnomalyClassification.HIGH_SUSPICION)
                .toList();
    }

    /**
     * Gets runners with moderate suspicion.
     *
     * @return list of runners with moderate suspicion
     */
    public List<RunnerAnomalyProfile> getModerateSuspicionRunners() {
        return runnerProfiles.stream()
            .filter(profile -> profile.classification() == AnomalyClassification.MODERATE_SUSPICION)
            .toList();
    }

    /**
     * Gets runners sorted by anomalies index (ascending: high suspicion first, no suspicion last).
     *
     * @return sorted list of runner profiles
     */
    public List<RunnerAnomalyProfile> getRunnersSortedByAnomaliesIndex() {
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
