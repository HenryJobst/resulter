package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.ResultListId;
import de.jobst.resulter.domain.analysis.AnomalyAnalysis;

import java.util.List;

/**
 * Service for detecting statistically significant speed anomalies that may indicate anomaly (abbreviations).
 */
public interface AnomalyDetectionService {

    AnomalyAnalysis analyzeAnomaly(
        ResultListId resultListId,
        List<Long> filterPersonIds);
}

