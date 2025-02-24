package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

import java.util.List;

@ValueObject
public record ClassResultScores(ClassResultShortName classResultShortName,
                                List<PersonWithScore> personWithScores) implements Comparable<ClassResultScores> {

    @Override
    public int compareTo(@NonNull ClassResultScores o) {
        return this.classResultShortName.compareTo(o.classResultShortName());
    }
}
