package de.jobst.resulter.adapter.driver.web.dto;

public record OrganisationStatisticsDto(
        OrganisationDto organisation,
        int runnerCount,
        int totalStarts,
        int nonScoringStarts,
        double startsPerRunner,
        double nonScoringStartsPerRunner,
        double nonScoringRatio
) {
}
