package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.domain.aggregations.OrganisationStatistics;

public record OrganisationStatisticsDto(
        OrganisationDto organisation,
        int runnerCount,
        int totalStarts,
        int nonScoringStarts,
        double startsPerRunner,
        double nonScoringStartsPerRunner,
        double nonScoringRatio
) {
    public static OrganisationStatisticsDto from(
            OrganisationStatistics stats,
            CountryService countryService,
            OrganisationService organisationService) {
        return new OrganisationStatisticsDto(
                OrganisationDto.from(stats.organisation(), countryService, organisationService),
                stats.runnerCount(),
                stats.totalStarts(),
                stats.nonScoringStarts(),
                stats.startsPerRunner(),
                stats.nonScoringStartsPerRunner(),
                stats.nonScoringRatio()
        );
    }
}
