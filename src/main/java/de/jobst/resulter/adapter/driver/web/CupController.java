package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.*;
import de.jobst.resulter.application.port.CupService;
import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.aggregations.CupDetailed;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
public class CupController {

    private final CupService cupService;
    private final EventService eventService;
    private final OrganisationService organisationService;
    private final CountryService countryService;
    private final EventCertificateService eventCertificateService;

    @Autowired
    public CupController(
            CupService cupService,
            EventService eventService,
            OrganisationService organisationService,
            CountryService countryService,
            EventCertificateService eventCertificateService) {
        this.cupService = cupService;
        this.eventService = eventService;
        this.organisationService = organisationService;
        this.countryService = countryService;
        this.eventCertificateService = eventCertificateService;
    }

    @GetMapping("/cup_types")
    public ResponseEntity<List<CupTypeDto>> handleCupTypes() {
        return ResponseEntity.ok(
                Arrays.stream(CupType.values()).map(CupTypeDto::from).toList());
    }

    @GetMapping("/cup/all")
    public ResponseEntity<List<CupDto>> getAllCups() {
        List<Cup> cups = cupService.findAll();
        return ResponseEntity.ok(
                cups.stream().map(x -> CupDto.from(x, eventService)).toList());
    }

    @GetMapping("/cup")
    public ResponseEntity<Page<CupDto>> searchCups(
            @RequestParam(required = false) Optional<String> filter, Pageable pageable) {
        Page<Cup> cups = cupService.findAll(
                filter.orElse(null),
                pageable != null
                        ? FilterAndSortConverter.mapOrderProperties(pageable, CupDto::mapOrdersDtoToDomain)
                        : Pageable.unpaged());
        return ResponseEntity.ok(new PageImpl<>(
                cups.getContent().stream()
                        .map(x -> CupDto.from(x, eventService))
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

    @NonNull
    private CupDetailedDto createCupDetailedDto(CupDetailed cupDetailed) {
        return CupDetailedDto.from(ObjectUtils.isNotEmpty(cupDetailed.getId()) ? cupDetailed.getId().value() : 0,
            cupDetailed.getName().value(),
            CupTypeDto.from(cupDetailed.getType()),
            cupDetailed.getEventIds().stream().map(x -> EventKeyDto.from(eventService.getById(x))).toList(),
            cupDetailed.getEventRacesCupScore()
                .stream()
                .map(x -> EventRacesCupScoreDto.from(x, organisationService, countryService, eventCertificateService))
                .toList(),
            cupDetailed.getType().isGroupedByOrganisation() ?
            cupDetailed.getOverallOrganisationScores()
                .stream()
                .map(entry -> new OrganisationScoreDto(OrganisationDto.from(entry.organisation(),
                    countryService,
                    organisationService),
                    entry.score(),
                    entry.personWithScores().stream().map(PersonWithScoreDto::from).toList()))
                .toList() :
            List.of(),
            cupDetailed.getType().isGroupedByOrganisation() ?
            List.of() :
            cupDetailed.getAggregatedPersonScoresList()
                .stream()
                .map(it -> new AggregatedPersonScoresDto(it.classResultShortName().value(),
                    it.personWithScoreList().stream().map(PersonWithScoreDto::from).toList()))
                .toList());
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
        return ResponseEntity.ok(cupScoreLists.stream().map(CupScoreListDto::from).toList());
    }
}
