package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.CheatingAnalysis;

import java.util.List;

public record CheatingAnalysisDto(
    Long resultListId,
    Long eventId,
    List<RunnerCheatingProfileDto> runnerProfiles
) {

    public static CheatingAnalysisDto from(CheatingAnalysis cheatingAnalysis) {
        return new CheatingAnalysisDto(
            cheatingAnalysis.resultListId().value(),
            cheatingAnalysis.eventId().value(),
            cheatingAnalysis.runnerProfiles().stream().map(RunnerCheatingProfileDto::from).toList())
        ;
    }
}
