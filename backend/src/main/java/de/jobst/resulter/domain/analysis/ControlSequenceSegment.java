package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.ControlCode;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.List;

@ValueObject
public record ControlSequenceSegment(
        List<ControlCode> controls,
        List<RunnerSplit> runnerSplits,
        List<String> classes
) {}
