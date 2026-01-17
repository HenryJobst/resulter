package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.EventRacesCupScoreDto;
import de.jobst.resulter.domain.aggregations.EventRacesCupScore;
import org.springframework.stereotype.Component;

@Component
public class EventRacesCupScoreMapper {

    private final EventMapper eventMapper;
    private final RaceOrganisationGroupedCupScoreMapper raceOrganisationGroupedCupScoreMapper;

    public EventRacesCupScoreMapper(
            EventMapper eventMapper, RaceOrganisationGroupedCupScoreMapper raceOrganisationGroupedCupScoreMapper) {
        this.eventMapper = eventMapper;
        this.raceOrganisationGroupedCupScoreMapper = raceOrganisationGroupedCupScoreMapper;
    }

    public EventRacesCupScoreDto toDto(EventRacesCupScore eventRacesCupScore, Boolean hasSplitTimes) {
        return new EventRacesCupScoreDto(
                eventMapper.toDto(eventRacesCupScore.event(), hasSplitTimes),
                eventRacesCupScore.raceOrganisationGroupedCupScores().stream()
                        .map(raceOrganisationGroupedCupScoreMapper::toDto)
                        .toList(),
                RaceClassResultGroupedCupScoreMapper.toDtos(eventRacesCupScore.raceClassResultGroupedCupScores()));
    }
}
