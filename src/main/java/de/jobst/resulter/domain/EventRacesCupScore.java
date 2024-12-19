package de.jobst.resulter.domain;

import java.util.List;

public record EventRacesCupScore(Event event,
                                 List<RaceOrganisationGroupedCupScore> raceOrganisationGroupedCupScores,
                                 List<RaceClassResultGroupedCupScore> raceClassResultGroupedCupScores) {}
