package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.aggregations.CupOverallStatistics;

public record CupOverallStatisticsDto(
        int totalRunners,
        int totalOrganisations,
        int totalStarts,
        int totalNonScoringStarts,
        double runnersPerOrganisation,
        double startsPerOrganisation,
        double nonScoringStartsPerOrganisation,
        double startsPerRunner,
        double nonScoringStartsPerRunner
) {
    public static CupOverallStatisticsDto from(CupOverallStatistics stats) {
        return new CupOverallStatisticsDto(
                stats.totalRunners(),
                stats.totalOrganisations(),
                stats.totalStarts(),
                stats.totalNonScoringStarts(),
                stats.runnersPerOrganisation(),
                stats.startsPerOrganisation(),
                stats.nonScoringStartsPerOrganisation(),
                stats.startsPerRunner(),
                stats.nonScoringStartsPerRunner()
        );
    }
}
