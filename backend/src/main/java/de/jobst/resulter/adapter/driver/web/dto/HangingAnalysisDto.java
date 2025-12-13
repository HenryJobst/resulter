package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.HangingAnalysis;

import java.util.List;

public record HangingAnalysisDto(
        Long resultListId,
        Long eventId,
        List<RunnerHangingProfileDto> runnerProfiles,
        HangingStatisticsDto statistics
) {

    public static HangingAnalysisDto from(HangingAnalysis analysis) {
        return new HangingAnalysisDto(
                analysis.resultListId().value(),
                analysis.eventId().value(),
                analysis.runnerProfiles().stream()
                        .map(RunnerHangingProfileDto::from)
                        .toList(),
                HangingStatisticsDto.from(analysis.statistics())
        );
    }
}
