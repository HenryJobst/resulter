package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.ClassResultShortName;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.ResultListId;
import lombok.Getter;
import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

import java.util.List;

@ValueObject
@Getter
public class SplitTimeAnalysis {

    @NonNull
    private final ResultListId resultListId;

    @NonNull
    private final EventId eventId;

    @NonNull
    private final ClassResultShortName classResultShortName;

    @NonNull
    private final List<ControlSegment> controlSegments;

    public SplitTimeAnalysis(
            @NonNull ResultListId resultListId,
            @NonNull EventId eventId,
            @NonNull ClassResultShortName classResultShortName,
            @NonNull List<ControlSegment> controlSegments) {
        this.resultListId = resultListId;
        this.eventId = eventId;
        this.classResultShortName = classResultShortName;
        this.controlSegments = controlSegments;
    }
}
