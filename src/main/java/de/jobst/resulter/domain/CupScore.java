package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

public record CupScore(PersonId personId, ClassResultShortName classResultShortName, double score)
    implements Comparable<CupScore> {

    public static CupScore of(PersonId personId, ClassResultShortName classResultShortName, double score) {
        return new CupScore(personId, classResultShortName, score);
    }

    @Override
    public int compareTo(@NonNull CupScore o) {
        int val = Double.compare(score, o.score);
        if (val == 0) {
            val = personId.compareTo(o.personId);
        }
        if (val == 0) {
            val = classResultShortName.compareTo(o.classResultShortName);
        }
        return val;
    }

}
