package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.ControlCode;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record AnomaliesIndexInformation(
        int legNumber,
        ControlCode fromControl,
        ControlCode toControl,
        PerformanceIndex pi,
        AnomaliesIndex anomaliesIndex,
        CheatingClassification classification
) {
    /**
     * @param legNumber            Leg number for the anomalies index (0-based)
     * @param fromControl          Control code where the anomalies segment starts
     * @param toControl            Control code where the anomalies segment ends
     * @param pi                   Performance Index on the segment
     * @param anomaliesIndex       Anomalies index
     * @param classification       Classification
     */
    public AnomaliesIndexInformation {
    }

    /**
     * Checks if this represents a high suspicion segment.
     *
     * @return true if classification is HIGH_SUSPICION
     */
    public boolean isHighSuspicion() {
        return classification == CheatingClassification.HIGH_SUSPICION;
    }

    /**
     * Checks if this represents a moderate suspicion.
     *
     * @return true if classification is MODERATE_SUSPICION
     */
    public boolean isModerateSuspicion() {
        return classification == CheatingClassification.MODERATE_SUSPICION;
    }

}
