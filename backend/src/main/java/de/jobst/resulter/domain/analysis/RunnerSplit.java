package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.PersonId;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record RunnerSplit(
        PersonId personId,
        String personName,
        String classResultShortName,
        Integer position,
        Double splitTimeSeconds,
        Double timeBehindLeader,
        boolean reversed
) implements Comparable<RunnerSplit> {

    @Override
    public int compareTo(RunnerSplit o) {
        return Double.compare(this.splitTimeSeconds, o.splitTimeSeconds);
    }
}
