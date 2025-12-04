package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.ClassResultShortName;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.ResultListId;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.List;

@ValueObject
public record SplitTimeAnalysis(ResultListId resultListId, EventId eventId, ClassResultShortName classResultShortName,
                                List<ControlSegment> controlSegments) {
}
