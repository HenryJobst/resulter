package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.ResultListId;
import de.jobst.resulter.domain.analysis.HangingAnalysis;

import java.util.List;

/**
 * Service for detecting hanging behavior where runners (passengers) follow faster runners (bus drivers)
 * and achieve better performance than their normal baseline.
 *
 * <p>Hanging detection identifies segments where:</p>
 * <ol>
 *   <li>Temporal proximity: Runner punched within 30s after another runner</li>
 *   <li>Performance hierarchy: The followed runner was faster on the segment</li>
 *   <li>Performance improvement: Runner performed significantly better than their normal PI</li>
 * </ol>
 */
public interface HangingDetectionService {

    /**
     * Analyzes hanging behavior for a result list.
     *
     * @param resultListId    The result list to analyze
     * @param filterPersonIds Optional list of person IDs to filter (empty list = analyze all runners)
     * @return Complete hanging analysis with runner profiles and statistics
     */
    HangingAnalysis analyzeHanging(
            ResultListId resultListId,
            List<Long> filterPersonIds
    );
}
