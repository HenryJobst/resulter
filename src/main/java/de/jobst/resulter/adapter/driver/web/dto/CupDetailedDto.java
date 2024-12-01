package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.CupDetailed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

@Slf4j
public record CupDetailedDto(Long id, String name, CupTypeDto type, List<EventKeyDto> events,
                             List<EventRacesCupScoreDto> eventRacesCupScores,
                             List<OrganisationScoreDto> overallOrganisationScores) {

    static public CupDetailedDto from(CupDetailed cup) {
        return new CupDetailedDto(ObjectUtils.isNotEmpty(cup.getId()) ? cup.getId().value() : 0,
            cup.getName().value(),
            CupTypeDto.from(cup.getType()),
            cup.getEvents().stream().map(EventKeyDto::from).toList(),
            cup.getEventRacesCupScore().stream().map(EventRacesCupScoreDto::from).toList(),
            cup.getOverallOrganisationScores()
                .stream()
                .map(entry -> new OrganisationScoreDto(OrganisationDto.from(entry.organisation()),
                    entry.score(),
                    entry.personWithScores().stream().map(PersonWithScoreDto::from).toList()
                ))
                .toList());
    }
}
