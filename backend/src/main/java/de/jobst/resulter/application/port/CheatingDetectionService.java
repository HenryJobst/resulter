package de.jobst.resulter.application.port;

import de.jobst.resulter.application.analysis.SegmentKey;
import de.jobst.resulter.domain.ResultListId;
import de.jobst.resulter.domain.analysis.AnomaliesIndex;
import de.jobst.resulter.domain.analysis.CheatingAnalysis;
import de.jobst.resulter.domain.analysis.MentalResilienceAnalysis;
import de.jobst.resulter.domain.analysis.PerformanceIndex;
import de.jobst.resulter.domain.PersonId;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Service for detecting statistically significant speed anomalies that may indicate cheating (abbreviations).
 */
public interface CheatingDetectionService {

    CheatingAnalysis analyzeCheating(
        ResultListId resultListId,
        List<Long> filterPersonIds);
}

