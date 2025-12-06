package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.MentalResilienceAnalysis;

import java.util.List;

/**
 * DTO for complete Mental Resilience Index analysis.
 */
public record MentalResilienceAnalysisDto(
        Long resultListId,
        Long eventId,
        List<RunnerMentalProfileDto> runnerProfiles,
        MriStatisticsDto statistics
) {
    /**
     * Converts domain analysis to DTO.
     *
     * @param analysis domain analysis
     * @return DTO
     */
    public static MentalResilienceAnalysisDto from(MentalResilienceAnalysis analysis) {
        return new MentalResilienceAnalysisDto(
                analysis.resultListId().value(),
                analysis.eventId().value(),
                analysis.runnerProfiles().stream()
                        .map(RunnerMentalProfileDto::from)
                        .toList(),
                MriStatisticsDto.from(analysis.statistics())
        );
    }
}
