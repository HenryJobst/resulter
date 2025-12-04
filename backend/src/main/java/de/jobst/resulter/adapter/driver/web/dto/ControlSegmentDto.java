package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.ControlSegment;

import java.util.List;

public record ControlSegmentDto(
        String fromControl,
        String toControl,
        String segmentLabel,
        List<RunnerSplitDto> runnerSplits,
        List<String> classes
) {

    public static ControlSegmentDto from(ControlSegment segment) {
        String fromControl = segment.getFromControl().value();
        String toControl = segment.getToControl().value();
        String segmentLabel = fromControl + " → " + toControl;

        List<RunnerSplitDto> runnerSplits = segment.getRunnerSplits().stream()
                .map(RunnerSplitDto::from)
                .toList();

        return new ControlSegmentDto(
                fromControl,
                toControl,
                segmentLabel,
                runnerSplits,
                segment.getClasses()
        );
    }
}
