package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.AggregatedPersonScoresDto;
import de.jobst.resulter.adapter.driver.web.dto.CupDetailedDto;
import de.jobst.resulter.adapter.driver.web.dto.CupStatisticsDto;
import de.jobst.resulter.adapter.driver.web.dto.CupTypeDto;
import de.jobst.resulter.adapter.driver.web.dto.EventDto;
import de.jobst.resulter.adapter.driver.web.dto.EventKeyDto;
import de.jobst.resulter.application.port.CupDetailedBatchResult;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.aggregations.CupDetailed;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;

public class CupDetailedMapper {

    public static CupDetailedDto toDto(CupDetailedBatchResult batchResult) {
        CupDetailed cupDetailed = batchResult.cupDetailed();
        Map<EventId, Event> eventMap = batchResult.eventMap();
        Map<EventId, Boolean> hasSplitTimesMap = batchResult.hasSplitTimesMap();
        Map<OrganisationId, Organisation> organisationMap = batchResult.organisationMap();
        Map<EventCertificateId, EventCertificate> certificateMap = batchResult.certificateMap();
        Map<CountryId, Country> countryMap = batchResult.countryMap();
        Map<OrganisationId, Organisation> childOrganisationMap = batchResult.childOrganisationMap();

        List<EventKeyDto> eventKeyDtos = cupDetailed.getEventIds().stream()
                .map(eventMap::get)
                .filter(java.util.Objects::nonNull)
                .map(EventMapper::toKeyDto)
                .sorted()
                .toList();

        Map<Long, de.jobst.resulter.adapter.driver.web.dto.PersonDto> personsDto =
                cupDetailed.getPersonsById().entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> entry.getKey().value(),
                                entry -> PersonMapper.toDto(entry.getValue())));

        CupStatisticsDto cupStatisticsDto =
                CupStatisticsMapper.toDto(cupDetailed.getCupStatistics(), countryMap, childOrganisationMap);

        Map<EventId, EventDto> eventDtosById = EventMapper.toDtos(
                        cupDetailed.getEventRacesCupScore().stream()
                                .map(de.jobst.resulter.domain.aggregations.EventRacesCupScore::event)
                                .distinct()
                                .toList(),
                        hasSplitTimesMap,
                        organisationMap,
                        certificateMap)
                .stream()
                .collect(Collectors.toMap(dto -> EventId.of(dto.id()), Function.identity()));

        return new CupDetailedDto(
                ObjectUtils.isNotEmpty(cupDetailed.getId()) ? cupDetailed.getId().value() : 0,
                cupDetailed.getName().value(),
                CupTypeDto.from(cupDetailed.getType()),
                eventKeyDtos,
                EventRacesCupScoreMapper.toDtos(
                        cupDetailed.getEventRacesCupScore(), eventDtosById, countryMap, childOrganisationMap),
                cupDetailed.getType().isGroupedByOrganisation()
                        ? OrganisationScoreMapper.toDtos(
                                cupDetailed.getOverallOrganisationScores(), countryMap, childOrganisationMap)
                        : List.of(),
                cupDetailed.getType().isGroupedByOrganisation()
                        ? List.of()
                        : cupDetailed.getAggregatedPersonScoresList().stream()
                                .map(it -> new AggregatedPersonScoresDto(
                                        it.classResultShortName().value(),
                                        PersonWithScoreMapper.toDtos(it.personWithScoreList())))
                                .toList(),
                personsDto,
                cupStatisticsDto);
    }
}
