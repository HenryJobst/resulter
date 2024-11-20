package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.CupDetailed;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;
import java.util.Map;

public record CupDetailedDto(Long id, String name, CupTypeDto type, List<EventKeyDto> events,
                             List<EventRacesCupScoreDto> eventRacesCupScoreDto,
                             List<Map.Entry<OrganisationDto, Double>> overallOrganisationScores) {

    static public CupDetailedDto from(CupDetailed cup) {
        return new CupDetailedDto(ObjectUtils.isNotEmpty(cup.getId()) ? cup.getId().value() : 0,
            cup.getName().value(),
            CupTypeDto.from(cup.getType()),
            cup.getEvents().stream().map(EventKeyDto::from).toList(),
            cup.getEventRacesCupScore().stream().map(EventRacesCupScoreDto::from).toList(),
            cup.getOverallOrganisationScores()
                .stream()
                .map(entry -> Map.entry(OrganisationDto.from(entry.getKey()), entry.getValue()))
                .toList());
    }
}
