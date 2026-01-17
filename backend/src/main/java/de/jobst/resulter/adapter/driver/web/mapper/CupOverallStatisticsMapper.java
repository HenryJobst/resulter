package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.CupOverallStatisticsDto;
import de.jobst.resulter.domain.aggregations.CupOverallStatistics;

public class CupOverallStatisticsMapper {

    private CupOverallStatisticsMapper() {}

    public static CupOverallStatisticsDto toDto(CupOverallStatistics stats) {
        return new CupOverallStatisticsDto(
                stats.totalRunners(),
                stats.totalOrganisations(),
                stats.totalStarts(),
                stats.totalNonScoringStarts(),
                stats.runnersPerOrganisation(),
                stats.startsPerOrganisation(),
                stats.nonScoringStartsPerOrganisation(),
                stats.startsPerRunner(),
                stats.nonScoringStartsPerRunner());
    }
}
