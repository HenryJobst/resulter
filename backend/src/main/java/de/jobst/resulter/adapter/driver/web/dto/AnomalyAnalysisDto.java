package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.AnomalyAnalysis;

import java.util.List;

public record AnomalyAnalysisDto(
    Long resultListId,
    Long eventId,
    List<RunnerAnomalyProfileDto> runnerProfiles
) {

    public static AnomalyAnalysisDto from(AnomalyAnalysis anomalyAnalysis) {
        return new AnomalyAnalysisDto(
            anomalyAnalysis.resultListId().value(),
            anomalyAnalysis.eventId().value(),
            anomalyAnalysis.runnerProfiles().stream().map(RunnerAnomalyProfileDto::from).toList())
        ;
    }
}
