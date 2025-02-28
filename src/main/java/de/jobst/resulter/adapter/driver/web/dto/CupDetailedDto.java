package de.jobst.resulter.adapter.driver.web.dto;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public record CupDetailedDto(
        Long id,
        String name,
        CupTypeDto type,
        List<EventKeyDto> events,
        List<EventRacesCupScoreDto> eventRacesCupScores,
        List<OrganisationScoreDto> overallOrganisationScores,
        List<AggregatedPersonScoresDto> aggregatedPersonScores) {

    public static CupDetailedDto from(long cupId, String name, CupTypeDto cupType, List<EventKeyDto> eventKeyDtos,
                                      List<EventRacesCupScoreDto> eventRacesCupScoreDtos,
                                      List<OrganisationScoreDto> overallOrganisationScoreDtos,
                                      List<AggregatedPersonScoresDto> aggregatedPersonScoreDtos) {
        return new CupDetailedDto(
                cupId,
                name,
                cupType,
                eventKeyDtos,
                eventRacesCupScoreDtos,
                overallOrganisationScoreDtos,
                aggregatedPersonScoreDtos);
    }
}
