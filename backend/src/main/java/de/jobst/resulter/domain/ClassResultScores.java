package de.jobst.resulter.domain;

import java.util.List;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record ClassResultScores(ClassResultShortName classResultShortName, List<PersonWithScore> personWithScores)
        implements Comparable<ClassResultScores> {

    @Override
    public int compareTo(ClassResultScores o) {
        return this.classResultShortName.compareTo(o.classResultShortName());
    }
}
