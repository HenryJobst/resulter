package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.RaceNumber;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.List;

/**
 * Anomaly profile for a single runner.
 *
 * <p>Contains:</p>
 * <ul>
 *   <li>Runner identification (person ID, class, race number)</li>
 *   <li>Class statistics (runner count, reliability indicator)</li>
 *   <li>Normal performance baseline (Normal PI)</li>
 *   <li>Anomaly classification</li>
 * </ul>
 */
@ValueObject
public record RunnerAnomalyProfile(
        PersonId personId,
        String classResultShortName,
        RaceNumber raceNumber,
        int classRunnerCount,
        boolean reliableData,
        PerformanceIndex normalPI,
        Double minimumAnomaliesIndex,
        int minimumLegNumber,
        List<AnomaliesIndexInformation> anomaliesIndexInformations,
        AnomalyClassification classification
) implements Comparable<RunnerAnomalyProfile> {

    /**
     * Creates a runner anomaly profile.
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
     * @param classification        Overall anomaly classification
     */
    public RunnerAnomalyProfile {
    }

    /**
     * Compares runners by average MRI (ascending order: panic runners first, resigners last).
     *
     * @param other other runner profile
     * @return comparison result
     */
    @Override
    public int compareTo(RunnerAnomalyProfile other) {
        // Sort by anomalies index: negative (anomaly) first, positive last
        return Double.compare(this.minimumAnomaliesIndex, other.minimumAnomaliesIndex);
    }

    @Override
    public String toString() {
        return String.format("RunnerProfile(personId=%s, class=%s, normalPI=%.3f, minimumAnomaliesIndex=%.3f, " +
                             "%s)",
                personId, classResultShortName, normalPI.value(), minimumAnomaliesIndex, classification);
    }
}
