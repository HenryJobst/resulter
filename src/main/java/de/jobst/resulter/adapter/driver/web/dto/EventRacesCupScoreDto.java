package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.EventRacesCupScore;

import java.util.List;

public record EventRacesCupScoreDto(EventDto event,
                                    List<RaceOrganisationGroupedCupScoreDto> raceOrganisationGroupedCupScores,
                                    List<RaceClassResultGroupedCupScoreDto> raceClassResultGroupedCupScores) {

    public static EventRacesCupScoreDto from(EventRacesCupScore eventRacesCupScore) {
        return new EventRacesCupScoreDto(
            EventDto.from(eventRacesCupScore.event()),
            eventRacesCupScore.raceOrganisationGroupedCupScores().stream().map(RaceOrganisationGroupedCupScoreDto::from).toList(),
            eventRacesCupScore.raceClassResultGroupedCupScores().stream().map(RaceClassResultGroupedCupScoreDto::from).toList()
            );
    }
}
