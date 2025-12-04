package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.ControlCode;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.List;

@ValueObject
public record ControlSegment(
        ControlCode fromControl,
        ControlCode toControl,
        List<RunnerSplit> runnerSplits,
        List<String> classes,
        boolean bidirectional
) {
}
