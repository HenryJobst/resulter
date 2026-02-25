package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.PersonId;
import org.jmolecules.ddd.annotation.ValueObject;

import java.util.List;

@ValueObject
public record SequenceRunnerSplit(
        PersonId personId,
        String classResultShortName,
        Integer position,
        Double splitTimeSeconds,
        Double timeBehindLeader,
        List<Double> legSplitTimesSeconds
) implements Comparable<SequenceRunnerSplit> {

    @Override
    public int compareTo(SequenceRunnerSplit o) {
        return Double.compare(this.splitTimeSeconds, o.splitTimeSeconds);
    }
}
