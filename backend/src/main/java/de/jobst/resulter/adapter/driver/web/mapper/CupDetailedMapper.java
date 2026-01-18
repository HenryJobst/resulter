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

    @Deprecated(since = "4.6.2", forRemoval = true)
    private Boolean hasSplitTimes(Event event) {
        return resultListService.findByEventId(event.getId()).stream().anyMatch(resultList -> !splitTimeListRepository
                .findByResultListId(resultList.getId())
                .isEmpty());
    }

    private Map<EventId, Boolean> batchHasSplitTimes(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        // Batch load result lists for all events
        Set<EventId> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());

        List<ResultList> allResultLists = eventIds.stream()
                .flatMap(eventId -> resultListService.findByEventId(eventId).stream())
                .toList();

        // Batch load split time lists for all result lists
        Set<ResultListId> resultListIds =
                allResultLists.stream().map(ResultList::getId).collect(Collectors.toSet());

        Map<ResultListId, Boolean> hasSplitTimesPerResultList = resultListIds.stream()
                .collect(Collectors.toMap(resultListId -> resultListId, resultListId -> !splitTimeListRepository
                        .findByResultListId(resultListId)
                        .isEmpty()));

        // Group by event and check if any result list has split times
        Map<EventId, List<ResultList>> resultListsByEvent =
                allResultLists.stream().collect(Collectors.groupingBy(ResultList::getEventId));

        return eventIds.stream()
                .collect(Collectors.toMap(
                        eventId -> eventId, eventId -> resultListsByEvent.getOrDefault(eventId, List.of()).stream()
                                .anyMatch(resultList ->
                                        hasSplitTimesPerResultList.getOrDefault(resultList.getId(), false))));
    }
}
