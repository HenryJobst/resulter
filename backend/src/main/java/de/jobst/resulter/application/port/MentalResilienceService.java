package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.ResultListId;
import de.jobst.resulter.domain.analysis.MentalResilienceAnalysis;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

import java.util.List;

/**
 * Service for analyzing mental resilience of orienteering runners.
 *
 * <p>Analyzes how runners mentally react after making navigation mistakes by calculating
 * the Mental Resilience Index (MRI) for each mistake-reaction pair.</p>
 *
 * <p>Current implementation supports result list (event) level analysis.
 * Future extensions will support cup and year level aggregations.</p>
 */
@PrimaryPort
public interface MentalResilienceService {

    /**
     * Analyzes mental resilience for all runners in a result list.
     *
     * <p>The analysis:</p>
     * <ul>
     *   <li>Calculates Performance Index (PI) for each segment</li>
     *   <li>Identifies mistakes (PI > 1.30 = 30% slower than best)</li>
     *   <li>Calculates Mental Resilience Index (MRI) for reactions to mistakes</li>
     *   <li>Classifies runners as Panic/Ice-Man/Resigner based on average MRI</li>
     *   <li>Provides aggregate statistics</li>
     * </ul>
     *
     * @param resultListId    Result list to analyze
     * @param filterPersonIds Optional list of person IDs to filter (empty = all runners)
     * @return Mental resilience analysis with runner profiles and statistics
     */
    MentalResilienceAnalysis analyzeMentalResilience(
            ResultListId resultListId,
            List<Long> filterPersonIds
    );
}
