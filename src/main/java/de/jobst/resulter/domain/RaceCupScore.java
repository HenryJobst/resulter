package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

import java.util.List;

public record RaceCupScore(Race race, List<OrganisationScore> organisationScores) implements Comparable<RaceCupScore> {

    @Override
    public int compareTo(@NonNull RaceCupScore o) {
        return this.race.compareTo(o.race);
    }
}
