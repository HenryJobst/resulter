package de.jobst.resulter.adapter.driver.web.dto;

public record CupOverallStatisticsDto(
        int totalRunners,
        int totalOrganisations,
        int totalStarts,
        int totalNonScoringStarts,
        double runnersPerOrganisation,
        double startsPerOrganisation,
        double nonScoringStartsPerOrganisation,
        double startsPerRunner,
        double nonScoringStartsPerRunner) {}
