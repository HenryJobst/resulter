package de.jobst.resulter.domain.aggregations;

import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.PersonWithScore;
import java.util.List;
import org.springframework.lang.NonNull;

public record OrganisationScore(Organisation organisation, Double score, List<PersonWithScore> personWithScores)
        implements Comparable<OrganisationScore> {

    @Override
    public int compareTo(@NonNull OrganisationScore o) {
        int val = this.score.compareTo(o.score()) * -1;
        if (val == 0) {
            val = this.organisation().compareTo(o.organisation());
        }
        return val;
    }
}
