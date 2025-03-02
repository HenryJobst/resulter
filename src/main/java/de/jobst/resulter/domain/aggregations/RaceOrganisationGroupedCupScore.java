package de.jobst.resulter.domain.aggregations;

import de.jobst.resulter.domain.Race;
import java.util.List;
import org.springframework.lang.NonNull;

public record RaceOrganisationGroupedCupScore(Race race, @NonNull List<OrganisationScore> organisationScores)
        implements Comparable<RaceOrganisationGroupedCupScore> {

    @Override
    public int compareTo(@NonNull RaceOrganisationGroupedCupScore o) {
        return this.race.compareTo(o.race);
    }
}
