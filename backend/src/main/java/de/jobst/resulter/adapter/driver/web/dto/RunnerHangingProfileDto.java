package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.RunnerHangingProfile;

import java.util.List;

public record RunnerHangingProfileDto(
        Long personId,
        String classResultShortName,
        Integer raceNumber,
        Double startTime,
        int classRunnerCount,
        boolean reliableData,
        double normalPI,
        List<HangingPairDto> hangingPairs,
        double averageHangingIndex,
        String classification,
        int totalNonMistakeSegments,
        int hangingCount,
        double hangingPercentage
) {

    public static RunnerHangingProfileDto from(RunnerHangingProfile profile) {
        return new RunnerHangingProfileDto(
                profile.personId().value(),
                profile.classResultShortName(),
                profile.raceNumber().value().intValue(),
                profile.startTime().value(),
                profile.classRunnerCount(),
                profile.reliableData(),
                profile.normalPI().value(),
                profile.hangingPairs().stream()
                        .map(HangingPairDto::from)
                        .toList(),
                profile.averageHangingIndex(),
                profile.classification().name(),
                profile.totalNonMistakeSegments(),
                profile.getHangingCount(),
                profile.getHangingPercentage()
        );
    }
}
