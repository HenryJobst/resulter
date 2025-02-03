package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

import java.util.List;

public record RaceOrganisationGroupedCupScore(Race race, @NonNull List<OrganisationScore> organisationScores) implements Comparable<RaceOrganisationGroupedCupScore> {

    @Override
    public int compareTo(@NonNull RaceOrganisationGroupedCupScore o) {
        return this.race.compareTo(o.race);
    }
}
