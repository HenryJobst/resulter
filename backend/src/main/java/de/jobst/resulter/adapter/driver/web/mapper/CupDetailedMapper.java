package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.AggregatedPersonScoresDto;
import de.jobst.resulter.adapter.driver.web.dto.CupDetailedDto;
import de.jobst.resulter.adapter.driver.web.dto.CupStatisticsDto;
import de.jobst.resulter.adapter.driver.web.dto.CupTypeDto;
import de.jobst.resulter.adapter.driver.web.dto.EventKeyDto;
import de.jobst.resulter.adapter.driver.web.dto.PersonDto;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.ResultListService;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.ResultList;
import de.jobst.resulter.domain.ResultListId;
import de.jobst.resulter.domain.aggregations.CupDetailed;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

@Component
public class CupDetailedMapper {

    private final EventService eventService;
    private final ResultListService resultListService;
    private final SplitTimeListRepository splitTimeListRepository;
    private final CupStatisticsMapper cupStatisticsMapper;
    private final EventRacesCupScoreMapper eventRacesCupScoreMapper;
    private final OrganisationScoreMapper organisationScoreMapper;

    public CupDetailedMapper(
            EventService eventService,
            ResultListService resultListService,
            SplitTimeListRepository splitTimeListRepository,
            CupStatisticsMapper cupStatisticsMapper,
            EventRacesCupScoreMapper eventRacesCupScoreMapper,
            OrganisationScoreMapper organisationScoreMapper) {
        this.eventService = eventService;
        this.resultListService = resultListService;
        this.splitTimeListRepository = splitTimeListRepository;
        this.cupStatisticsMapper = cupStatisticsMapper;
        this.eventRacesCupScoreMapper = eventRacesCupScoreMapper;
        this.organisationScoreMapper = organisationScoreMapper;
    }

    public CupDetailedDto toDto(CupDetailed cupDetailed) {
        // Batch-load all Events
        Map<EventId, Event> eventMap = eventService
                .findAllById(cupDetailed.getEventIds().stream().toList())
                .stream()
                .collect(Collectors.toMap(Event::getId, event -> event));

        List<EventKeyDto> eventKeyDtos = cupDetailed.getEventIds().stream()
                .map(eventMap::get)
                .filter(java.util.Objects::nonNull)
                .map(EventMapper::toKeyDto)
                .sorted()
                .toList();

        // Convert Map<PersonId, Person> to Map<Long, PersonDto>
        Map<Long, PersonDto> personsDto = cupDetailed.getPersonsById().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().value(), entry -> PersonMapper.toDto(entry.getValue())));

        // Convert cup statistics
        CupStatisticsDto cupStatisticsDto = cupStatisticsMapper.toDto(cupDetailed.getCupStatistics());

        // Batch-load hasSplitTimes status for all events
        List<Event> eventsFromCupScore = cupDetailed.getEventRacesCupScore().stream()
                .map(x -> x.event())
                .distinct()
                .toList();
        Map<EventId, Boolean> hasSplitTimesMap = batchHasSplitTimes(eventsFromCupScore);

        return new CupDetailedDto(
                ObjectUtils.isNotEmpty(cupDetailed.getId())
                        ? cupDetailed.getId().value()
                        : 0,
                cupDetailed.getName().value(),
                CupTypeDto.from(cupDetailed.getType()),
                eventKeyDtos,
                cupDetailed.getEventRacesCupScore().stream()
                        .map(x -> eventRacesCupScoreMapper.toDto(
                                x, hasSplitTimesMap.getOrDefault(x.event().getId(), false)))
                        .toList(),
                cupDetailed.getType().isGroupedByOrganisation()
                        ? organisationScoreMapper.toDtos(cupDetailed.getOverallOrganisationScores())
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

    private Map<EventId, Boolean> batchHasSplitTimes(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        Set<EventId> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        Map<EventId, List<ResultList>> resultListsByEvent = resultListService.findAllByEventIds(eventIds);

        Set<ResultListId> resultListIds = resultListsByEvent.values().stream()
                .flatMap(List::stream)
                .map(ResultList::getId)
                .collect(Collectors.toSet());
        Set<ResultListId> resultListIdsWithSplitTimes = splitTimeListRepository.existsByResultListIds(resultListIds);

        return eventIds.stream()
                .collect(Collectors.toMap(
                        eventId -> eventId, eventId -> resultListsByEvent.getOrDefault(eventId, List.of()).stream()
                                .anyMatch(resultList -> resultListIdsWithSplitTimes.contains(resultList.getId()))));
    }
}
