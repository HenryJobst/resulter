package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.RunnerAnomalyProfile;

import java.util.List;

/**
 * DTO for a runner's complete anomaly resilience profile.
 */
public record RunnerAnomalyProfileDto(
        Long personId,
        String classResultShortName,
        Integer raceNumber,
        Integer classRunnerCount,
        Boolean reliableData,
        Double normalPI,
        Double minimumAnomaliesIndex,
        int minimumAnomaliesLegNumber,
        List<AnomaliesIndexInformationDto> anomaliesIndexes,
        String classification,
        Integer totalSegments
) {
    /**
     * Converts domain runner profile to DTO.
     *
     * @param profile       domain runner profile
     * @param totalSegments total number of segments in the course
     * @return DTO
     */
    public static RunnerAnomalyProfileDto from(RunnerAnomalyProfile profile, int totalSegments) {
        return new RunnerAnomalyProfileDto(
                profile.personId().value(),
                profile.classResultShortName(),
                profile.raceNumber().value().intValue(),
                profile.classRunnerCount(),
                profile.reliableData(),
                profile.normalPI().value(),
                profile.minimumAnomaliesIndex(),
                profile.minimumLegNumber(),
                profile.anomaliesIndexInformations().stream().map(AnomaliesIndexInformationDto::from).toList(),
                profile.classification().name(),
                totalSegments
        );
    }

    /**
     * Converts domain runner profile to DTO without total segments count.
     *
     * @param profile domain runner profile
     * @return DTO with totalSegments set to 0
     */
    public static RunnerAnomalyProfileDto from(RunnerAnomalyProfile profile) {
        return from(profile, 0);
    }
}
