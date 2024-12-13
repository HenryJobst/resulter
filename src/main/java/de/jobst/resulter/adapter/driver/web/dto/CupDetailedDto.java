package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.ClassResultShortName;
import de.jobst.resulter.domain.CupDetailed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public record CupDetailedDto(Long id, String name, CupTypeDto type, List<EventKeyDto> events,
                             List<EventRacesCupScoreDto> eventRacesCupScores,
                             List<OrganisationScoreDto> overallOrganisationScores,
                             Map<ClassResultShortName, List<PersonWithScoreDto>> classResultShortNameScores) {

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
                    entry.personWithScores().stream().map(PersonWithScoreDto::from).toList()))
                .toList(),
            cup.getClassResultShortNameScores()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                    it -> it.getValue().stream().map(PersonWithScoreDto::from).toList())));
    }
}
