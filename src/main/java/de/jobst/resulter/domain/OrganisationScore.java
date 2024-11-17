package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

import java.util.List;

public record OrganisationScore(Organisation organisation, Double score,
                                List<PersonWithScore> personWithScores) implements Comparable<OrganisationScore> {

    @Override
    public int compareTo(@NonNull OrganisationScore o) {
        int val = this.score.compareTo(o.score()) * -1;
        if (val == 0) {
            val = this.organisation().compareTo(o.organisation());
        }
        return val;
    }
}
