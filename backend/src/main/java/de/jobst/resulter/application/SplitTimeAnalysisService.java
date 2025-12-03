package de.jobst.resulter.application;

import de.jobst.resulter.domain.ResultListId;
import de.jobst.resulter.domain.analysis.SplitTimeAnalysis;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

import java.util.List;
import java.util.Optional;

@PrimaryPort
public interface SplitTimeAnalysisService {

    /**
     * Analyze split times for a result list
     *
     * @param resultListId The result list to analyze
     * @param mergeBidirectional If true, merge forward and reverse directions
     * @param filterNames Optional list of person names to filter (null or empty = all)
     * @return Split time analysis or empty if no data
     */
    Optional<SplitTimeAnalysis> analyzeSplitTimesRanking(
        ResultListId resultListId,
        boolean mergeBidirectional,
        List<String> filterNames
                                                        );
}
