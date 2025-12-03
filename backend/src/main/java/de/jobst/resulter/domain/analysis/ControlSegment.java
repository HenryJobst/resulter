package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.ControlCode;
import lombok.Getter;
import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

import java.util.List;

@ValueObject
@Getter
public class ControlSegment {

    @NonNull
    private final ControlCode fromControl;

    @NonNull
    private final ControlCode toControl;

    @NonNull
    private final List<RunnerSplit> runnerSplits;

    public ControlSegment(
            @NonNull ControlCode fromControl,
            @NonNull ControlCode toControl,
            @NonNull List<RunnerSplit> runnerSplits) {
        this.fromControl = fromControl;
        this.toControl = toControl;
        this.runnerSplits = runnerSplits;
    }
}
