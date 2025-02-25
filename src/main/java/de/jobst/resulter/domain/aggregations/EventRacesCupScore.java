package de.jobst.resulter.domain.aggregations;

import de.jobst.resulter.domain.Event;
import java.util.List;

public record EventRacesCupScore(
        Event event,
        List<RaceOrganisationGroupedCupScore> raceOrganisationGroupedCupScores,
        List<RaceClassResultGroupedCupScore> raceClassResultGroupedCupScores) {}
