package de.jobst.resulter.domain.analysis;

/**
 * Classification of speed anomalies based on the Anomalies Index (AI).
 * Anomalies can indicate potential issues like shortcuts, GPS errors, or timing system problems.
 */
public enum AnomalyClassification {
    NO_SUSPICION,
    MODERATE_SUSPICION,
    HIGH_SUSPICION,
    NO_DATA
}
