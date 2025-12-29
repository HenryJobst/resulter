package de.jobst.resulter.domain.aggregations;

/**
 * Overall statistics for the entire cup
 * Maps to PDF requirements:
 * - Anzahl der Läufer (totalRunners): 325
 * - Anzahl der Vereine (totalOrganisations): 18
 * - Gesamtstarts (totalStarts): 661, davon 41 (6%) ohne Wertung
 * - Läufer pro Verein: 18.06
 * - Starts pro Verein: 36.72, davon 2.28 ohne Wertung
 * - Starts pro Läufer: 2.03, davon 0.13 ohne Wertung
 */
public record CupOverallStatistics(
        int totalRunners,           // Unique persons who participated
        int totalOrganisations,     // Count of organizations
        int totalStarts,            // All starts (OK + non-OK)
        int totalNonScoringStarts,  // Starts without scoring (DNF, etc.)
        double runnersPerOrganisation,
        double startsPerOrganisation,
        double nonScoringStartsPerOrganisation,
        double startsPerRunner,
        double nonScoringStartsPerRunner
) {
    /**
     * Factory method to calculate derived fields from base counts
     */
    public static CupOverallStatistics of(
            int totalRunners,
            int totalOrganisations,
            int totalStarts,
            int totalNonScoringStarts
    ) {
        double runnersPerOrg = totalOrganisations > 0
                ? (double) totalRunners / totalOrganisations
                : 0.0;
        double startsPerOrg = totalOrganisations > 0
                ? (double) totalStarts / totalOrganisations
                : 0.0;
        double nonScoringStartsPerOrg = totalOrganisations > 0
                ? (double) totalNonScoringStarts / totalOrganisations
                : 0.0;
        double startsPerRunner = totalRunners > 0
                ? (double) totalStarts / totalRunners
                : 0.0;
        double nonScoringStartsPerRunner = totalRunners > 0
                ? (double) totalNonScoringStarts / totalRunners
                : 0.0;

        return new CupOverallStatistics(
                totalRunners,
                totalOrganisations,
                totalStarts,
                totalNonScoringStarts,
                runnersPerOrg,
                startsPerOrg,
                nonScoringStartsPerOrg,
                startsPerRunner,
                nonScoringStartsPerRunner
        );
    }
}
