package de.jobst.resulter.application.port;

import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.ResultListId;
import de.jobst.resulter.domain.analysis.SplitTimeAnalysis;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

import java.util.List;

@PrimaryPort
public interface SplitTimeRankingService {

    /**
     * Analyze split times for a result list
     *
     * @param resultListId The result list to analyze
     * @param mergeBidirectional If true, merge forward and reverse directions
     * @param filterPersonIds Optional list of person IDs to filter (null or empty = all)
     * @param filterIntersection If true, show only segments where ALL filtered persons appear together
     * @return List of split time analyses (one per class) or empty list if no data
     */
    List<SplitTimeAnalysis> analyzeSplitTimesRanking(
        ResultListId resultListId,
        boolean mergeBidirectional,
        List<Long> filterPersonIds,
        boolean filterIntersection
    );

    /**
     * Get all persons that participated in a result list
     *
     * @param resultListId The result list ID
     * @return List of persons sorted by family name, given name
     */
    List<Person> getPersonsForResultList(ResultListId resultListId);
}
