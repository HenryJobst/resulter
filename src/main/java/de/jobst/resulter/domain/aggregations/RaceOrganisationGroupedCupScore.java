package de.jobst.resulter.domain.aggregations;

import de.jobst.resulter.domain.Race;

import java.util.List;

public record RaceOrganisationGroupedCupScore(Race race, List<OrganisationScore> organisationScores)
        implements Comparable<RaceOrganisationGroupedCupScore> {

    @Override
    public int compareTo(RaceOrganisationGroupedCupScore o) {
        return this.race.compareTo(o.race);
    }
}
