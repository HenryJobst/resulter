package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.EventService;
import de.jobst.resulter.domain.CupDetailed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

@Slf4j
public record CupDetailedDto(Long id, String name, CupTypeDto type, List<EventKeyDto> events,
                             List<EventRacesCupScoreDto> eventRacesCupScores,
                             List<OrganisationScoreDto> overallOrganisationScores,
                             List<AggregatedPersonScoresDto> aggregatedPersonScores) {

    static public CupDetailedDto from(CupDetailed cup, EventService eventService) {
        return new CupDetailedDto(ObjectUtils.isNotEmpty(cup.getId()) ? cup.getId().value() : 0,
            cup.getName().value(),
            CupTypeDto.from(cup.getType()),
            cup.getEventIds().stream().map(x -> EventKeyDto.from(eventService.getById(x))).toList(),
            cup.getEventRacesCupScore().stream().map(EventRacesCupScoreDto::from).toList(),
            cup.getType().isGroupedByOrganisation() ?
            cup.getOverallOrganisationScores()
                .stream()
                .map(entry -> new OrganisationScoreDto(OrganisationDto.from(entry.organisation()),
                    entry.score(),
                    entry.personWithScores().stream().map(PersonWithScoreDto::from).toList()))
                .toList() :
            List.of(),
            cup.getType().isGroupedByOrganisation() ?
            List.of() :
            cup.getAggregatedPersonScoresList()
                .stream()
                .map(it -> new AggregatedPersonScoresDto(it.classResultShortName().value(),
                    it.personWithScoreList().stream().map(PersonWithScoreDto::from).toList()))
                .toList());
    }
}
