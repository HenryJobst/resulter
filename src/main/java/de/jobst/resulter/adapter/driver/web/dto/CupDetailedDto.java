package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.domain.aggregations.CupDetailed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

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

    public static CupDetailedDto from(
            CupDetailed cup,
            EventService eventService,
            OrganisationService organisationService,
            CountryService countryService,
            EventCertificateService eventCertificateService) {
        return new CupDetailedDto(
                ObjectUtils.isNotEmpty(cup.getId()) ? cup.getId().value() : 0,
                cup.getName().value(),
                CupTypeDto.from(cup.getType()),
                cup.getEventIds().stream()
                        .map(x -> EventKeyDto.from(eventService.getById(x)))
                        .toList(),
                cup.getEventRacesCupScore().stream()
                        .map(x -> EventRacesCupScoreDto.from(
                                x, organisationService, countryService, eventCertificateService))
                        .toList(),
                cup.getType().isGroupedByOrganisation()
                        ? cup.getOverallOrganisationScores().stream()
                                .map(entry -> new OrganisationScoreDto(
                                        OrganisationDto.from(entry.organisation(), countryService, organisationService),
                                        entry.score(),
                                        entry.personWithScores().stream()
                                                .map(PersonWithScoreDto::from)
                                                .toList()))
                                .toList()
                        : List.of(),
                cup.getType().isGroupedByOrganisation()
                        ? List.of()
                        : cup.getAggregatedPersonScoresList().stream()
                                .map(it -> new AggregatedPersonScoresDto(
                                        it.classResultShortName().value(),
                                        it.personWithScoreList().stream()
                                                .map(PersonWithScoreDto::from)
                                                .toList()))
                                .toList());
    }
}
