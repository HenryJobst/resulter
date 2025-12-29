package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.domain.aggregations.CupStatistics;

import java.util.List;

public record CupStatisticsDto(
        CupOverallStatisticsDto overallStatistics,
        List<OrganisationStatisticsDto> organisationStatistics
) {
    public static CupStatisticsDto from(
            CupStatistics cupStatistics,
            CountryService countryService,
            OrganisationService organisationService) {
        return new CupStatisticsDto(
                CupOverallStatisticsDto.from(cupStatistics.overallStatistics()),
                cupStatistics.organisationStatistics().stream()
                        .map(stats -> OrganisationStatisticsDto.from(stats, countryService, organisationService))
                        .toList()
        );
    }
}
