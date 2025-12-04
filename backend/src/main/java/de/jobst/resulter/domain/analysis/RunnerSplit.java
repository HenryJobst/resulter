package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.PersonId;
import org.jmolecules.ddd.annotation.ValueObject;

/**
 * Represents a single runner's split time for a control segment.
 * Used for ranking runners on individual segments.
 * Note: Person name is not included to reduce data duplication -
 * frontend should fetch person details separately.
 */
@ValueObject
public record RunnerSplit(
        PersonId personId,
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
