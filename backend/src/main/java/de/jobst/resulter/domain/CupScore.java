package de.jobst.resulter.domain;

import org.jmolecules.ddd.annotation.ValueObject;
import org.springframework.lang.NonNull;

@ValueObject
public record CupScore(
        PersonId personId, OrganisationId organisationId, ClassResultShortName classResultShortName, double score)
        implements Comparable<CupScore> {

    public static CupScore of(
            PersonId personId, OrganisationId organisationId, ClassResultShortName classResultShortName, double score) {
        return new CupScore(personId, organisationId, classResultShortName, score);
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
