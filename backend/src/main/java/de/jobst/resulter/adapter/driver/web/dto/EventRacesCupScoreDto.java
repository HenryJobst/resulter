package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.adapter.driver.web.mapper.EventMapper;
import de.jobst.resulter.adapter.driver.web.mapper.OrganisationScoreMapper;
import de.jobst.resulter.domain.aggregations.EventRacesCupScore;

import java.util.List;

public record EventRacesCupScoreDto(
        EventDto event,
        List<RaceOrganisationGroupedCupScoreDto> raceOrganisationGroupedCupScores,
        List<RaceClassResultGroupedCupScoreDto> raceClassResultGroupedCupScores) {

    public static EventRacesCupScoreDto from(
            EventRacesCupScore eventRacesCupScore,
            Boolean hasSplitTimes,
            OrganisationScoreMapper organisationScoreMapper,
            EventMapper eventMapper) {
        return new EventRacesCupScoreDto(
                eventMapper.toDto(eventRacesCupScore.event(), hasSplitTimes),
                eventRacesCupScore.raceOrganisationGroupedCupScores().stream()
                        .map(r -> RaceOrganisationGroupedCupScoreDto.from(r, organisationScoreMapper))
                        .toList(),
                eventRacesCupScore.raceClassResultGroupedCupScores().stream()
                        .map(RaceClassResultGroupedCupScoreDto::from)
                        .toList());
    }
}
