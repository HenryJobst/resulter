package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.EventRacesCupScoreDto;
import de.jobst.resulter.adapter.driver.web.dto.EventDto;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.aggregations.EventRacesCupScore;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EventRacesCupScoreMapper {

    private final RaceOrganisationGroupedCupScoreMapper raceOrganisationGroupedCupScoreMapper;

    public EventRacesCupScoreMapper(RaceOrganisationGroupedCupScoreMapper raceOrganisationGroupedCupScoreMapper) {
        this.raceOrganisationGroupedCupScoreMapper = raceOrganisationGroupedCupScoreMapper;
    }

    public EventRacesCupScoreDto toDto(EventRacesCupScore eventRacesCupScore, EventDto eventDto) {
        return new EventRacesCupScoreDto(
                eventDto,
                eventRacesCupScore.raceOrganisationGroupedCupScores().stream()
                        .map(raceOrganisationGroupedCupScoreMapper::toDto)
                        .toList(),
                RaceClassResultGroupedCupScoreMapper.toDtos(eventRacesCupScore.raceClassResultGroupedCupScores()));
    }

    public List<EventRacesCupScoreDto> toDtos(
            List<EventRacesCupScore> eventRacesCupScores,
            Map<EventId, EventDto> eventDtosById) {
        return eventRacesCupScores.stream()
                .filter(score -> eventDtosById.containsKey(score.event().getId()))
                .map(score -> toDto(score, eventDtosById.get(score.event().getId())))
                .toList();
    }
}
