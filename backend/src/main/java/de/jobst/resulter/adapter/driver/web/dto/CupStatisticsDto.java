package de.jobst.resulter.adapter.driver.web.dto;

import java.util.List;

public record CupStatisticsDto(
        CupOverallStatisticsDto overallStatistics, List<OrganisationStatisticsDto> organisationStatistics) {}
