package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.ControlSequenceSegment;

import java.util.List;

public record ControlSequenceSegmentDto(
        List<String> controls,
        String segmentLabel,
        List<RunnerSplitDto> runnerSplits,
        List<String> classes
) {

    public static ControlSequenceSegmentDto from(ControlSequenceSegment segment) {
        List<String> controls = segment.controls().stream().map(c -> c.value()).toList();
        String segmentLabel = String.join(" → ", controls);

        List<RunnerSplitDto> runnerSplits = segment.runnerSplits().stream()
                .map(RunnerSplitDto::from)
                .toList();

        return new ControlSequenceSegmentDto(
                controls,
                segmentLabel,
                runnerSplits,
                segment.classes()
        );
    }
}
