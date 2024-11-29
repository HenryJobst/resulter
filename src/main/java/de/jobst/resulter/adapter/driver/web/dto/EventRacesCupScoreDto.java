package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.EventRacesCupScore;

import java.util.List;

public record EventRacesCupScoreDto(EventDto event, List<RaceCupScoreDto> raceCupScores) {

    public static EventRacesCupScoreDto from(EventRacesCupScore eventRacesCupScore) {
        return new EventRacesCupScoreDto(
            EventDto.from(eventRacesCupScore.event()),
            eventRacesCupScore.eventRaces().stream().map(x -> RaceCupScoreDto.from(x)).toList());
    }
}
