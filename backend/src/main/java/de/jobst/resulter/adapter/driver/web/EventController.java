package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.DisciplineDto;
import de.jobst.resulter.adapter.driver.web.dto.EventCertificateDto;
import de.jobst.resulter.adapter.driver.web.dto.EventDto;
import de.jobst.resulter.adapter.driver.web.dto.EventResultsDto;
import de.jobst.resulter.adapter.driver.web.dto.EventStatusDto;
import de.jobst.resulter.adapter.driver.web.mapper.EventMapper;
import de.jobst.resulter.adapter.driver.web.mapper.EventResultsMapper;
import de.jobst.resulter.application.port.*;
import de.jobst.resulter.application.util.FilterAndSortConverter;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import de.jobst.resulter.domain.util.ResponseNotFoundException;
import jakarta.validation.Valid;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Validated
public class EventController {

    private final EventService eventService;
    private final EventQueryService eventQueryService;
    private final ResultListService resultListService;
    private final MediaFileService mediaFileService;
    private final CupRepository cupRepository;
    public EventController(
            EventService eventService,
            EventQueryService eventQueryService,
            ResultListService resultListService,
            MediaFileService mediaFileService,
            CupRepository cupRepository) {
        this.eventService = eventService;
        this.eventQueryService = eventQueryService;
        this.resultListService = resultListService;
        this.mediaFileService = mediaFileService;
        this.cupRepository = cupRepository;
    }

    @GetMapping("/event/all")
    public ResponseEntity<List<EventDto>> getAllEvents() {
        EventBatchResult result = eventQueryService.findAll();
        List<EventDto> dtos = EventMapper.toDtos(
                result.events(), result.hasSplitTimesMap(), result.organisationMap(), result.certificateMap());
        return ResponseEntity.ok(dtos.stream().sorted(Comparator.reverseOrder()).toList());
    }

    @GetMapping("/event")
    public ResponseEntity<Page<EventDto>> searchEvents(
            @RequestParam Optional<String> filter, @Nullable Pageable pageable) {
        Pageable mappedPageable = pageable != null
                ? FilterAndSortConverter.mapOrderProperties(pageable, EventDto::mapOrdersDtoToDomain)
                : Pageable.unpaged();
        EventBatchResult result = eventQueryService.findAll(filter.orElse(null), mappedPageable);
        List<EventDto> dtos = EventMapper.toDtos(
                result.events(), result.hasSplitTimesMap(), result.organisationMap(), result.certificateMap());
        return ResponseEntity.ok(new PageImpl<>(
                dtos,
                FilterAndSortConverter.mapOrderProperties(result.resolvedPageable(), EventDto::mapOrdersDomainToDto),
                result.totalElements()));
    }

    @GetMapping("/event_status")
    public ResponseEntity<List<EventStatusDto>> handleEventTypes() {
        List<EventStatusDto> eventStatus =
                Arrays.stream(EventStatus.values()).map(EventStatusDto::from).toList();
        return ResponseEntity.ok(eventStatus);
    }

    @GetMapping("/discipline")
    public ResponseEntity<List<DisciplineDto>> handleDisciplines() {
        List<DisciplineDto> disciplines =
                Arrays.stream(Discipline.values()).map(DisciplineDto::from).toList();
        return ResponseEntity.ok(disciplines);
    }

    @PostMapping("/event")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventDto> createEvent(@RequestBody EventDto eventDto) {
        if (eventDto.name().isEmpty()) {
            throw new IllegalArgumentException("name is not set");
        }
        Event event = eventService.createEvent(
                eventDto.name(),
                ObjectUtils.isNotEmpty(eventDto.startTime())
                        ? ZonedDateTime.parse(eventDto.startTime(), DateTimeFormatter.ISO_DATE_TIME)
                        : null,
                eventDto.organisations() == null
                        ? new HashSet<>()
                        : eventDto.organisations().stream()
                                .map(x -> OrganisationId.of(x.id()))
                                .collect(Collectors.toSet()),
                eventDto.discipline().id(),
                eventDto.aggregateScore());
        if (null == event) {
            throw new ResponseNotFoundException("Event could not be created");
        }
        return eventQueryService
                .findById(event.getId().value())
                .map(result -> EventMapper.toDtos(
                                result.events(),
                                result.hasSplitTimesMap(),
                                result.organisationMap(),
                                result.certificateMap())
                        .getFirst())
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseNotFoundException("Event could not be created"));
    }

    @GetMapping("/event/{id}")
    public ResponseEntity<EventDto> getEvent(@PathVariable Long id) {
        return eventQueryService
                .findById(id)
                .map(result -> EventMapper.toDtos(
                                result.events(),
                                result.hasSplitTimesMap(),
                                result.organisationMap(),
                                result.certificateMap())
                        .getFirst())
                .map(ResponseEntity::ok)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @PutMapping("/event/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long id, @RequestBody EventDto eventDto) {
        Event event = eventService.updateEvent(
                EventId.of(id),
                EventName.of(eventDto.name()),
                ObjectUtils.isNotEmpty(eventDto.startTime())
                        ? DateTime.of(ZonedDateTime.parse(eventDto.startTime(), DateTimeFormatter.ISO_DATE_TIME))
                        : null,
                EventStatus.fromValue(eventDto.state().id()),
                eventDto.organisations() == null
                        ? new HashSet<>()
                        : eventDto.organisations().stream()
                                .map(x -> OrganisationId.of(x.id()))
                                .collect(Collectors.toSet()),
                eventDto.certificate() != null
                        ? EventCertificateId.of(eventDto.certificate().id())
                        : null,
                Discipline.fromValue(eventDto.discipline().id()),
                eventDto.aggregateScore());
        return eventQueryService
                .findById(event.getId().value())
                .map(result -> EventMapper.toDtos(
                                result.events(),
                                result.hasSplitTimesMap(),
                                result.organisationMap(),
                                result.certificateMap())
                        .getFirst())
                .map(ResponseEntity::ok)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @DeleteMapping("/event/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(EventId.of(id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/event/{id}/results")
    public ResponseEntity<EventResultsDto> getEventResults(@PathVariable Long id) {
        Event event = eventService.findById(EventId.of(id)).orElseThrow(ResourceNotFoundException::new);
        boolean eventHasCup = !cupRepository.findByEvent(event.getId()).isEmpty();
        return ResponseEntity.ok(
                EventResultsMapper.toDto(event, eventHasCup, resultListService.findByEventId(event.getId())));
    }

    @PutMapping("/event/{id}/certificate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> getCertificate(
            @PathVariable Long id, @Valid @RequestBody EventCertificateDto eventCertificateDto) {

        Event event = eventService.getById(EventId.of(id));
        MediaFile blankCertificate = mediaFileService.getById(
                MediaFileId.of(eventCertificateDto.blankCertificate().id()));

        EventCertificate eventCertificate = EventCertificate.of(
                EventCertificateId.empty().value(),
                eventCertificateDto.name(),
                event.getId(),
                eventCertificateDto.layoutDescription(),
                blankCertificate.getId(),
                eventCertificateDto.primary());
        CertificateService.Certificate certificate = resultListService.createCertificate(event, eventCertificate);
        if (null != certificate) {
            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION.toLowerCase(),
                            "inline; filename=\"" + certificate.filename() + "\"")
                    .contentLength(certificate.size())
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(certificate.resource());
        } else {
            throw new ResponseNotFoundException("Certificate could not be created");
        }
    }
}
