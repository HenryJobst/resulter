package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.*;
import de.jobst.resulter.adapter.driver.web.mapper.CupStatisticsMapper;
import de.jobst.resulter.adapter.driver.web.mapper.OrganisationScoreMapper;
import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.application.port.CupService;
import de.jobst.resulter.application.util.FilterAndSortConverter;
import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.aggregations.CupDetailed;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class CupController {

    private final CupService cupService;
    private final EventService eventService;
    private final OrganisationService organisationService;
    private final CountryService countryService;
    private final EventCertificateService eventCertificateService;
    private final de.jobst.resulter.application.port.ResultListService resultListService;
    private final de.jobst.resulter.application.port.SplitTimeListRepository splitTimeListRepository;
    private final OrganisationScoreMapper organisationScoreMapper;
    private final CupStatisticsMapper cupStatisticsMapper;

    public CupController(
            CupService cupService,
            EventService eventService,
            OrganisationService organisationService,
            CountryService countryService,
            EventCertificateService eventCertificateService,
            de.jobst.resulter.application.port.ResultListService resultListService,
            de.jobst.resulter.application.port.SplitTimeListRepository splitTimeListRepository,
            OrganisationScoreMapper organisationScoreMapper,
            CupStatisticsMapper cupStatisticsMapper) {
        this.cupService = cupService;
        this.eventService = eventService;
        this.organisationService = organisationService;
        this.countryService = countryService;
        this.eventCertificateService = eventCertificateService;
        this.resultListService = resultListService;
        this.splitTimeListRepository = splitTimeListRepository;
        this.organisationScoreMapper = organisationScoreMapper;
        this.cupStatisticsMapper = cupStatisticsMapper;
    }

    @GetMapping("/cup_types")
    public ResponseEntity<List<CupTypeDto>> handleCupTypes() {
        return ResponseEntity.ok(
                Arrays.stream(CupType.values()).map(CupTypeDto::from).toList());
    }

    @GetMapping("/cup/all")
    public ResponseEntity<List<CupDto>> getAllCups() {
        List<Cup> cups = cupService.findAll();

        // Batch-load all Events for all Cups
        List<EventId> allEventIds = cups.stream()
                .flatMap(cup -> cup.getEventIds().stream())
                .distinct()
                .toList();
        Map<EventId, Event> eventMap = eventService.findAllById(allEventIds).stream()
                .collect(Collectors.toMap(Event::getId, event -> event));

        return ResponseEntity.ok(
                cups.stream().map(x -> CupDto.from(x, eventMap)).toList());
    }

    @GetMapping("/cup")
    public ResponseEntity<Page<CupDto>> searchCups(
            @RequestParam(required = false) Optional<String> filter, @Nullable Pageable pageable) {
        Page<Cup> cups = cupService.findAll(
                filter.orElse(null),
                pageable != null
                        ? FilterAndSortConverter.mapOrderProperties(pageable, CupDto::mapOrdersDtoToDomain)
                        : Pageable.unpaged());

        // Batch-load all Events for all Cups in the page
        List<EventId> allEventIds = cups.getContent().stream()
                .flatMap(cup -> cup.getEventIds().stream())
                .distinct()
                .toList();
        Map<EventId, Event> eventMap = eventService.findAllById(allEventIds).stream()
                .collect(Collectors.toMap(Event::getId, event -> event));

        return ResponseEntity.ok(new PageImpl<>(
                cups.getContent().stream()
                        .map(x -> CupDto.from(x, eventMap))
                        .toList(),
                FilterAndSortConverter.mapOrderProperties(cups.getPageable(), CupDto::mapOrdersDomainToDto),
                cups.getTotalElements()));
    }

    @GetMapping("/cup/{id}")
    public ResponseEntity<CupDto> getCup(@PathVariable Long id) {
        return ResponseEntity.ok(CupDto.from(cupService.getById(CupId.of(id)), eventService));
    }

    @GetMapping("/cup/{id}/results")
    public ResponseEntity<CupDetailedDto> getCupDetailed(@PathVariable Long id) {
        CupDetailed cupDetailed = cupService.getCupDetailed(CupId.of(id));
        return ResponseEntity.ok(createCupDetailedDto(cupDetailed));
    }

    private Boolean hasSplitTimes(Event event) {
        return resultListService.findByEventId(event.getId()).stream()
                .anyMatch(resultList -> !splitTimeListRepository.findByResultListId(resultList.getId()).isEmpty());
    }

    private CupDetailedDto createCupDetailedDto(CupDetailed cupDetailed) {
        // Batch-load all Events
        Map<EventId, Event> eventMap = eventService.findAllById(cupDetailed.getEventIds().stream().toList()).stream()
                .collect(Collectors.toMap(Event::getId, event -> event));

        List<EventKeyDto> eventKeyDtos = cupDetailed.getEventIds().stream()
                .map(eventMap::get)
                .filter(java.util.Objects::nonNull)
                .map(EventKeyDto::from)
                .sorted()
                .toList();

        // Convert Map<PersonId, Person> to Map<Long, PersonDto>
        Map<Long, PersonDto> personsDto = cupDetailed.getPersonsById().entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().value(), entry -> PersonDto.from(entry.getValue())));

        // Batch-load all Organisations (for OrganisationScores and Statistics)
        List<Organisation> organisationsForScores = cupDetailed.getOverallOrganisationScores().stream()
                .map(score -> score.organisation())
                .toList();
        List<Organisation> organisationsForStats = cupDetailed.getCupStatistics().organisationStatistics().stream()
                .map(stats -> stats.organisation())
                .toList();
        List<Organisation> allOrganisations = new java.util.ArrayList<>();
        allOrganisations.addAll(organisationsForScores);
        allOrganisations.addAll(organisationsForStats);

        Map<CountryId, Country> countryMap = countryService.batchLoadForOrganisations(allOrganisations);
        Map<OrganisationId, Organisation> orgMap = organisationService.batchLoadChildOrganisations(allOrganisations);

        // Convert cup statistics
        CupStatisticsDto cupStatisticsDto = cupStatisticsMapper.toDto(
                cupDetailed.getCupStatistics(),
                countryMap,
                orgMap);

        return CupDetailedDto.from(
                ObjectUtils.isNotEmpty(cupDetailed.getId())
                        ? cupDetailed.getId().value()
                        : 0,
                cupDetailed.getName().value(),
                CupTypeDto.from(cupDetailed.getType()),
                eventKeyDtos,
                cupDetailed.getEventRacesCupScore().stream()
                        .map(x -> EventRacesCupScoreDto.from(
                                x, organisationService, countryService, eventCertificateService, hasSplitTimes(x.event()), organisationScoreMapper))
                        .toList(),
                cupDetailed.getType().isGroupedByOrganisation()
                        ? cupDetailed.getOverallOrganisationScores().stream()
                                .map(entry -> organisationScoreMapper.toDto(entry, countryMap, orgMap))
                                .toList()
                        : List.of(),
                cupDetailed.getType().isGroupedByOrganisation()
                        ? List.of()
                        : cupDetailed.getAggregatedPersonScoresList().stream()
                                .map(it -> new AggregatedPersonScoresDto(
                                        it.classResultShortName().value(),
                                        it.personWithScoreList().stream()
                                                .map(PersonWithScoreDto::from)
                                                .toList()))
                                .toList(),
                personsDto,
                cupStatisticsDto);
    }

    @PutMapping("/cup/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CupDto> updateCup(@PathVariable Long id, @RequestBody CupDto cupDto) {
        Cup cup = cupService.updateCup(
                CupId.of(id),
                CupName.of(cupDto.name()),
                CupType.fromValue(cupDto.type().id()),
                Year.of(cupDto.year()),
                cupDto.events() == null
                        ? new ArrayList<>()
                        : cupDto.events().stream().map(x -> EventId.of(x.id())).toList());
        return ResponseEntity.ok(CupDto.from(cup, eventService));
    }

    @PostMapping("/cup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CupDto> createCup(@RequestBody CupDto cupDto) {
        Cup cup = cupService.createCup(
                cupDto.name(),
                CupType.fromValue(cupDto.type().id()),
                Year.of(cupDto.year()),
                cupDto.events() == null
                        ? new ArrayList<>()
                        : cupDto.events().stream()
                                .map(x -> EventId.of(x.id()))
                                .filter(ObjectUtils::isNotEmpty)
                                .toList());
        return ResponseEntity.ok(CupDto.from(cup, eventService));
    }

    @DeleteMapping("/cup/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> deleteCup(@PathVariable Long id) {
        cupService.deleteCup(CupId.of(id));
        return ResponseEntity.ok(Boolean.TRUE);
    }

    @PutMapping("/cup/{id}/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CupScoreListDto>> calculateCup(@PathVariable Long id) {
        List<CupScoreList> cupScoreLists = cupService.calculateScore(CupId.of(id));
        return ResponseEntity.ok(
                cupScoreLists.stream().map(CupScoreListDto::from).toList());
    }
}
