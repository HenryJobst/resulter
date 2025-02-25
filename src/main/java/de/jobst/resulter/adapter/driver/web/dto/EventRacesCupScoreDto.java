package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.OrganisationService;
import de.jobst.resulter.domain.aggregations.EventRacesCupScore;
import java.util.List;

public record EventRacesCupScoreDto(
        EventDto event,
        List<RaceOrganisationGroupedCupScoreDto> raceOrganisationGroupedCupScores,
        List<RaceClassResultGroupedCupScoreDto> raceClassResultGroupedCupScores) {

    public static EventRacesCupScoreDto from(
            EventRacesCupScore eventRacesCupScore, OrganisationService organisationService) {
        return new EventRacesCupScoreDto(
                EventDto.from(eventRacesCupScore.event(), organisationService),
                eventRacesCupScore.raceOrganisationGroupedCupScores().stream()
                        .map(RaceOrganisationGroupedCupScoreDto::from)
                        .toList(),
                eventRacesCupScore.raceClassResultGroupedCupScores().stream()
                        .map(RaceClassResultGroupedCupScoreDto::from)
                        .toList());
    }
}
