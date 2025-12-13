package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.ResultListId;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.List;

/**
 * Complete hanging detection analysis for a result list.
 *
 * <p>Contains all runner hanging profiles and aggregate statistics.</p>
 */
@ValueObject
public record HangingAnalysis(
        ResultListId resultListId,
        EventId eventId,
        List<RunnerHangingProfile> runnerProfiles,
        HangingStatistics statistics
) {
    /**
     * Creates a hanging detection analysis with immutable collections.
     *
     * @param resultListId  Result list identifier
     * @param eventId       Event identifier
     * @param runnerProfiles List of runner hanging profiles (immutable copy will be created)
     * @param statistics     Aggregate statistics
     */
    public HangingAnalysis {
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

    @Override
    public String toString() {
        return String.format("HangingAnalysis(resultList=%s, runners=%d, withHanging=%d, totalSegments=%d)",
                resultListId, getRunnerCount(), statistics.runnersWithHanging(),
                statistics.totalHangingSegments());
    }
}
