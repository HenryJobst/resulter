package de.jobst.resulter.domain.aggregations;

import de.jobst.resulter.domain.Organisation;

/**
 * Per-organization statistics
 * Maps to PDF table columns:
 * Verein | OLer | Starts | o.W. | Starts/OLer | o.W./OLer | o.W./Starts
 */
public record OrganisationStatistics(
        Organisation organisation,
        int runnerCount,              // OLer (orienteers from this org)
        int totalStarts,              // Total starts
        int nonScoringStarts,         // o.W. (ohne Wertung - without scoring)
        double startsPerRunner,       // Starts/OLer
        double nonScoringStartsPerRunner,  // o.W./OLer
        double nonScoringRatio        // o.W./Starts (as percentage, e.g., 0.06 for 6%)
) implements Comparable<OrganisationStatistics> {

    /**
     * Factory method to calculate ratios from base counts
     */
    public static OrganisationStatistics of(
            Organisation organisation,
            int runnerCount,
            int totalStarts,
            int nonScoringStarts
    ) {
        double startsPerRunner = runnerCount > 0
                ? (double) totalStarts / runnerCount
                : 0.0;
        double nonScoringStartsPerRunner = runnerCount > 0
                ? (double) nonScoringStarts / runnerCount
                : 0.0;
        double nonScoringRatio = totalStarts > 0
                ? (double) nonScoringStarts / totalStarts
                : 0.0;

        return new OrganisationStatistics(
                organisation,
                runnerCount,
                totalStarts,
                nonScoringStarts,
                startsPerRunner,
                nonScoringStartsPerRunner,
                nonScoringRatio
        );
    }

    @Override
    public int compareTo(OrganisationStatistics o) {
        // Sort by runner count descending (as per PDF)
        return Integer.compare(o.runnerCount, this.runnerCount);
    }
}
