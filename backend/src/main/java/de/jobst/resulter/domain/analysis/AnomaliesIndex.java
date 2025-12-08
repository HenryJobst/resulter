package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.application.analysis.SegmentKey;

/**
 * Represents the result of the cheating detection analysis for a single segment.
 */
public record AnomaliesIndex(
    int legNumber,
    SegmentKey segmentKey,
    PerformanceIndex piReal, // PI based on T_Referenz*
    PerformanceIndex piExpected, // Expected PI (Normal PI)
    double aiValue, // Anomalies Index (PI_Real / PI_Expected)
    CheatingClassification classification) {

    public static AnomaliesIndex of(int legNumber,
                                    SegmentKey segmentKey,
                                    PerformanceIndex piReal,
                                    PerformanceIndex piExpected,
                                    CheatingClassification classification) {
        double aiValue = piReal.value() / piExpected.value();
        return new AnomaliesIndex(legNumber, segmentKey, piReal, piExpected, aiValue, classification);
    }
}
