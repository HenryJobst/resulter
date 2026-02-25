package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.analysis.ControlSequenceSegment;

import java.util.List;

public record ControlSequenceSegmentDto(
        List<String> controls,
        String segmentLabel,
        List<SequenceRunnerSplitDto> runnerSplits,
        List<String> classes
) {

    public static ControlSequenceSegmentDto from(ControlSequenceSegment segment) {
        List<String> controls = segment.controls().stream().map(c -> c.value()).toList();
        String segmentLabel = String.join(" → ", controls);

        List<SequenceRunnerSplitDto> runnerSplits = segment.runnerSplits().stream()
                .map(SequenceRunnerSplitDto::from)
                .toList();

        return new ControlSequenceSegmentDto(
                controls,
                segmentLabel,
                runnerSplits,
                segment.classes()
        );
    }
}
