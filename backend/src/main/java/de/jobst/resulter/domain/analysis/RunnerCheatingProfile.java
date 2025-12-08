package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.RaceNumber;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.List;

/**
 * Cheating profile for a single runner.
 *
 * <p>Contains:</p>
 * <ul>
 *   <li>Runner identification (person ID, class, race number)</li>
 *   <li>Class statistics (runner count, reliability indicator)</li>
 *   <li>Normal performance baseline (Normal PI)</li>
 *   <li>Cheating classification</li>
 * </ul>
 */
@ValueObject
public record RunnerCheatingProfile(
        PersonId personId,
        String classResultShortName,
        RaceNumber raceNumber,
        int classRunnerCount,
        boolean reliableData,
        PerformanceIndex normalPI,
        Double minimumAnomaliesIndex,
        int minimumLegNumber,
        List<AnomaliesIndexInformation> anomaliesIndexInformations,
        CheatingClassification classification
) implements Comparable<RunnerCheatingProfile> {

    /**
     * Creates a runner cheating profile.
     *
     * @param personId              Person identifier
     * @param classResultShortName  Class short name
     * @param raceNumber            Race number
     * @param classRunnerCount      Number of runners in this class
     * @param reliableData          True if the class has enough runners (≥5) for reliable analysis
     * @param normalPI              Normal Performance Index (baseline)
     * @param minimumAnomaliesIndex Minimum anomalies index
     * @param minimumLegNumber      Leg number of the minimum anomalies index
     * @param anomaliesIndexInformations Anomalies index informations for all segments
     * @param classification        Overall cheating classification
     */
    public RunnerCheatingProfile {
    }

    /**
     * Compares runners by average MRI (ascending order: panic runners first, resigners last).
     *
     * @param other other runner profile
     * @return comparison result
     */
    @Override
    public int compareTo(RunnerCheatingProfile other) {
        // Sort by anomalies index: negative (cheating) first, positive last
        return Double.compare(this.minimumAnomaliesIndex, other.minimumAnomaliesIndex);
    }

    @Override
    public String toString() {
        return String.format("RunnerProfile(personId=%s, class=%s, normalPI=%.3f, minimumAnomaliesIndex=%.3f, " +
                             "%s)",
                personId, classResultShortName, normalPI.value(), minimumAnomaliesIndex, classification);
    }
}
