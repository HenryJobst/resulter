package de.jobst.resulter.adapter.driver.web.dto;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record CupDetailedDto(
        Long id,
        String name,
        CupTypeDto type,
        List<EventKeyDto> events,
        List<EventRacesCupScoreDto> eventRacesCupScores,
        List<OrganisationScoreDto> overallOrganisationScores,
        List<AggregatedPersonScoresDto> aggregatedPersonScores,
        Map<Long, PersonDto> persons,
        CupStatisticsDto cupStatistics) {

    public static CupDetailedDto from(
            long cupId,
            String name,
            CupTypeDto cupType,
            List<EventKeyDto> eventKeyDtos,
            List<EventRacesCupScoreDto> eventRacesCupScoreDtos,
            List<OrganisationScoreDto> overallOrganisationScoreDtos,
            List<AggregatedPersonScoresDto> aggregatedPersonScoreDtos,
            Map<Long, PersonDto> persons,
            CupStatisticsDto cupStatistics) {
        return new CupDetailedDto(
                cupId,
                name,
                cupType,
                eventKeyDtos,
                eventRacesCupScoreDtos,
                overallOrganisationScoreDtos,
                aggregatedPersonScoreDtos,
                persons,
                cupStatistics);
    }
}
