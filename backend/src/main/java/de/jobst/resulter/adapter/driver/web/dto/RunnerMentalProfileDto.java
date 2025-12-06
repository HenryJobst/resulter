package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.RunnerMentalProfile;

import java.util.List;

/**
 * DTO for a runner's complete mental resilience profile.
 */
public record RunnerMentalProfileDto(
        Long personId,
        String classResultShortName,
        Integer raceNumber,
        Integer classRunnerCount,
        Boolean reliableData,
        Double normalPI,
        List<MistakeReactionPairDto> mistakeReactions,
        Double averageMRI,
        String classification,
        Integer mistakeCount,
        Integer totalSegments
) {
    /**
     * Converts domain runner profile to DTO.
     *
     * @param profile       domain runner profile
     * @param totalSegments total number of segments in the course
     * @return DTO
     */
    public static RunnerMentalProfileDto from(RunnerMentalProfile profile, int totalSegments) {
        return new RunnerMentalProfileDto(
                profile.personId().value(),
                profile.classResultShortName(),
                profile.raceNumber().value().intValue(),
                profile.classRunnerCount(),
                profile.reliableData(),
                profile.normalPI().value(),
                profile.mistakeReactions().stream()
                        .map(MistakeReactionPairDto::from)
                        .toList(),
                profile.averageMRI(),
                profile.classification().getKey(),
                profile.getMistakeCount(),
                totalSegments
        );
    }

    /**
     * Converts domain runner profile to DTO without total segments count.
     *
     * @param profile domain runner profile
     * @return DTO with totalSegments set to 0
     */
    public static RunnerMentalProfileDto from(RunnerMentalProfile profile) {
        return from(profile, 0);
    }
}
