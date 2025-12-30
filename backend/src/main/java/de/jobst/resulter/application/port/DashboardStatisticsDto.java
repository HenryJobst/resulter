package de.jobst.resulter.application.port;

import org.jmolecules.architecture.hexagonal.Port;

/**
 * DTO for dashboard statistics.
 * Contains system-wide counts for various entities.
 */
@Port
public record DashboardStatisticsDto(
    Long eventCount,
    Long cupCount,
    Long personCount,
    Long organisationCount,  // Excludes OTHER type
    Long splitTimeCount,
    Long raceCount,
    Long resultListCount,
    Long certificateCount
) {
    /**
     * Compact canonical constructor for validation.
     * Ensures all count values are non-null and non-negative.
     */
    public DashboardStatisticsDto {
        if (eventCount < 0) {
            throw new IllegalArgumentException("eventCount cannot be null or negative");
        }
        if (cupCount < 0) {
            throw new IllegalArgumentException("cupCount cannot be null or negative");
        }
        if (personCount < 0) {
            throw new IllegalArgumentException("personCount cannot be null or negative");
        }
        if (organisationCount < 0) {
            throw new IllegalArgumentException("organisationCount cannot be null or negative");
        }
        if (splitTimeCount < 0) {
            throw new IllegalArgumentException("splitTimeCount cannot be null or negative");
        }
        if (raceCount < 0) {
            throw new IllegalArgumentException("raceCount cannot be null or negative");
        }
        if (resultListCount < 0) {
            throw new IllegalArgumentException("resultListCount cannot be null or negative");
        }
        if (certificateCount < 0) {
            throw new IllegalArgumentException("certificateCount cannot be null or negative");
        }
    }
}
