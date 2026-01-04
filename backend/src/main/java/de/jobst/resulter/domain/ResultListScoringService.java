package de.jobst.resulter.domain;

import java.time.LocalDate;
import java.util.Collection;

public class ResultListScoringService {

    /**
     * Determines if a ResultList is eligible for cup scoring based on event's aggregatedScore setting.
     * <p>
     * Rules:
     * - Single ResultList: Always scorable
     * - Multiple ResultLists:
     *   - If event.aggregatedScore is true: Only Race 0 (overall result) is scorable
     *   - If event.aggregatedScore is false: Only individual races (Race 1, 2, 3, ...) are scorable, NOT Race 0
     */
    public static boolean isScorableForCupCalculation(
        ResultList targetResultList,
        Collection<ResultList> allEventResultLists,
        Event event
    ) {
        if (allEventResultLists.size() == 1) {
            return true;
        }

        Byte raceNumber = targetResultList.getRaceNumber().value();
        if (raceNumber == null) {
            return false;
        }

        if (event.isAggregatedScore()) {
            // Only Race 0 (overall result) is scorable when aggregated scoring is enabled
            return raceNumber == 0;
        } else {
            // Only individual races are scorable when aggregated scoring is disabled (NOT Race 0)
            return raceNumber > 0;
        }
    }

    /**
     * Determines deletion scope for cup scores.
     *
     * @return true if deletion should be event-wide (same-day scenario),
     *         false if deletion should be ResultList-specific (multi-day scenario)
     */
    public static boolean shouldDeleteEventWide(
        Collection<ResultList> allEventResultLists
    ) {
        if (allEventResultLists.size() == 1) {
            return true;
        }

        long uniqueDates = countUniqueDates(allEventResultLists);
        return uniqueDates == 1;
    }

    private static long countUniqueDates(Collection<ResultList> resultLists) {
        return resultLists.stream()
            .map(rl -> rl.getCreateTime() != null ? extractDate(rl.getCreateTime()) : null)
            .distinct()
            .count();
    }

    private static LocalDate extractDate(java.time.ZonedDateTime zonedDateTime) {
        return zonedDateTime.toLocalDate();
    }
}
