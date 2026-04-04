package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.EventDto;
import de.jobst.resulter.adapter.driver.web.dto.EventRacesCupScoreDto;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.aggregations.EventRacesCupScore;
import java.util.List;
import java.util.Map;

public class EventRacesCupScoreMapper {

    public static EventRacesCupScoreDto toDto(
            EventRacesCupScore eventRacesCupScore,
            EventDto eventDto,
            Map<CountryId, Country> countryMap,
            Map<OrganisationId, Organisation> orgMap) {
        return new EventRacesCupScoreDto(
                eventDto,
                eventRacesCupScore.raceOrganisationGroupedCupScores().stream()
                        .map(score -> RaceOrganisationGroupedCupScoreMapper.toDto(score, countryMap, orgMap))
                        .toList(),
                RaceClassResultGroupedCupScoreMapper.toDtos(eventRacesCupScore.raceClassResultGroupedCupScores()));
    }

    public static List<EventRacesCupScoreDto> toDtos(
            List<EventRacesCupScore> eventRacesCupScores,
            Map<EventId, EventDto> eventDtosById,
            Map<CountryId, Country> countryMap,
            Map<OrganisationId, Organisation> orgMap) {
        return eventRacesCupScores.stream()
                .filter(score -> eventDtosById.containsKey(score.event().getId()))
                .map(score -> toDto(score, eventDtosById.get(score.event().getId()), countryMap, orgMap))
                .toList();
    }
}
