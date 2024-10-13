package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

public record CupScoreId(CupId cupId, PersonId personId, ClassResultShortName classResultShortName)
    implements Comparable<CupScoreId> {

    public static CupScoreId of(CupId cupId, PersonId personId, ClassResultShortName classResultShortName) {
        return new CupScoreId(cupId, personId, classResultShortName);
    }

    @Override
    public int compareTo(@NonNull CupScoreId o) {
        int val = classResultShortName.compareTo(o.classResultShortName);
        if (val == 0) {
            val = cupId.compareTo(o.cupId);
        }
        if (val == 0) {
            val = personId.compareTo(o.personId);
        }
        return val;
    }
}
