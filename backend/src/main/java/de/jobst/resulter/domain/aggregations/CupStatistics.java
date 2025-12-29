package de.jobst.resulter.domain.aggregations;

import java.util.List;

/**
 * Cup-level statistics including overall metrics and per-organization breakdown
 */
public record CupStatistics(
        CupOverallStatistics overallStatistics,
        List<OrganisationStatistics> organisationStatistics
) {
}
