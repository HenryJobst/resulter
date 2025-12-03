package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.SplitTimeAnalysis;

import java.util.List;

public record SplitTimeAnalysisDto(
        Long resultListId,
        Long eventId,
        String classResultShortName,
        List<ControlSegmentDto> controlSegments
) {

    public static SplitTimeAnalysisDto from(SplitTimeAnalysis analysis) {
        List<ControlSegmentDto> controlSegments = analysis.getControlSegments().stream()
                .map(ControlSegmentDto::from)
                .toList();

        return new SplitTimeAnalysisDto(
                analysis.getResultListId().value(),
                analysis.getEventId().value(),
                analysis.getClassResultShortName().value(),
                controlSegments
        );
    }
}
