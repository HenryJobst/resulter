package de.jobst.resulter.domain.analysis;

import de.jobst.resulter.domain.PersonId;
import lombok.Getter;
import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

@ValueObject
@Getter
public class RunnerSplit implements Comparable<RunnerSplit> {

    @NonNull
    private final PersonId personId;

    @NonNull
    private final String personName;

    @NonNull
    private final String classResultShortName;

    private final Integer position;

    private final Double splitTimeSeconds;

    private final Double timeBehindLeader;

    public RunnerSplit(
            @NonNull PersonId personId,
            @NonNull String personName,
            @NonNull String classResultShortName,
            Integer position,
            Double splitTimeSeconds,
            Double timeBehindLeader) {
        this.personId = personId;
        this.personName = personName;
        this.classResultShortName = classResultShortName;
        this.position = position;
        this.splitTimeSeconds = splitTimeSeconds;
        this.timeBehindLeader = timeBehindLeader;
    }

    @Override
    public int compareTo(@NonNull RunnerSplit o) {
        if (this.splitTimeSeconds == null && o.splitTimeSeconds == null) {
            return 0;
        }
        if (this.splitTimeSeconds == null) {
            return 1;
        }
        if (o.splitTimeSeconds == null) {
            return -1;
        }
        return Double.compare(this.splitTimeSeconds, o.splitTimeSeconds);
    }
}
