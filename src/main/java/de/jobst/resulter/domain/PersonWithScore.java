package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

@ValueObject
public record PersonWithScore(PersonId id, Double score, ClassResultShortName classResultShortName)
    implements Comparable<PersonWithScore> {

    @Override
    public int compareTo(@NonNull PersonWithScore o) {
        int val = Double.compare(score, o.score()) * -1;
        if (val == 0) {
            val = classResultShortName.compareTo(o.classResultShortName());
        }
        if (val == 0) {
            val = this.id.compareTo(o.id());
        }
        return val;
    }
}
