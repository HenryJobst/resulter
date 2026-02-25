package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.SplitTimeAnalysis;

import java.util.List;

public record SplitTimeAnalysisDto(
        Long resultListId,
        Long eventId,
        String classResultShortName,
        List<ControlSegmentDto> controlSegments,
        List<ControlSequenceSegmentDto> sequenceSegments
) {

    public static SplitTimeAnalysisDto from(SplitTimeAnalysis analysis) {
        List<ControlSegmentDto> controlSegments = analysis.controlSegments().stream()
                .map(ControlSegmentDto::from)
                .toList();

        List<ControlSequenceSegmentDto> sequenceSegments = analysis.sequenceSegments().stream()
                .map(ControlSequenceSegmentDto::from)
                .toList();

        return new SplitTimeAnalysisDto(
                analysis.resultListId().value(),
                analysis.eventId().value(),
                analysis.classResultShortName().value(),
                controlSegments,
                sequenceSegments
        );
    }
}
