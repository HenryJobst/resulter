package de.jobst.resulter.domain;

import java.util.List;
import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

@ValueObject
public record ClassResultScores(ClassResultShortName classResultShortName, List<PersonWithScore> personWithScores)
        implements Comparable<ClassResultScores> {

    @Override
    public int compareTo(@NonNull ClassResultScores o) {
        return this.classResultShortName.compareTo(o.classResultShortName());
    }
}
