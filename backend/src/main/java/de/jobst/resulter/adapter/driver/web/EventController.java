package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.DisciplineDto;
import de.jobst.resulter.adapter.driver.web.dto.EventCertificateDto;
import de.jobst.resulter.adapter.driver.web.dto.EventDto;
import de.jobst.resulter.adapter.driver.web.dto.EventResultsDto;
import de.jobst.resulter.adapter.driver.web.dto.EventStatusDto;
import de.jobst.resulter.adapter.driver.web.mapper.EventMapper;
import de.jobst.resulter.application.certificate.CertificateServiceImpl;
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
    private final ResultListService resultListService;
    private final OrganisationService organisationService;
    private final EventCertificateService eventCertificateService;
    private final MediaFileService mediaFileService;
    private final de.jobst.resulter.application.port.SplitTimeListRepository splitTimeListRepository;
    private final CupRepository cupRepository;
    private final EventMapper eventMapper;
    private final de.jobst.resulter.adapter.driver.web.mapper.EventResultsMapper eventResultsMapper;

    public EventController(
            EventService eventService,
            ResultListService resultListService,
            OrganisationService organisationService,
            EventCertificateService eventCertificateService,
            MediaFileService mediaFileService,
            de.jobst.resulter.application.port.SplitTimeListRepository splitTimeListRepository,
            CupRepository cupRepository,
            EventMapper eventMapper,
            de.jobst.resulter.adapter.driver.web.mapper.EventResultsMapper eventResultsMapper) {
        this.eventService = eventService;
        this.resultListService = resultListService;
        this.organisationService = organisationService;
        this.eventCertificateService = eventCertificateService;
        this.mediaFileService = mediaFileService;
        this.splitTimeListRepository = splitTimeListRepository;
        this.cupRepository = cupRepository;
        this.eventMapper = eventMapper;
        this.eventResultsMapper = eventResultsMapper;
    }

    @GetMapping("/event/all")
    public ResponseEntity<List<EventDto>> getAllEvents() {
        List<Event> events = eventService.findAll();
        Map<Long, Boolean> hasSplitTimesMap = batchHasSplitTimes(events);
        List<EventDto> eventDtos = eventMapper.toDtos(events, hasSplitTimesMap);
        return ResponseEntity.ok(
                eventDtos.stream().sorted(Comparator.reverseOrder()).toList());
        return ResponseEntity.ok(eventDtos.stream()
        Map<EventId, Boolean> hasSplitTimesByEventId = hasSplitTimesByEventId(events);
        return ResponseEntity.ok(events.stream()
                .map(x -> eventMapper.toDto(x, hasSplitTimes(x)))
                .map(x -> EventDto.from(x, organisationService, eventCertificateService,
                        hasSplitTimesByEventId.getOrDefault(x.getId(), Boolean.FALSE)))
                .sorted(Comparator.reverseOrder())
                .toList());
    }

    private Boolean hasSplitTimes(Event event) {
        return resultListService.findByEventId(event.getId()).stream().anyMatch(resultList -> !splitTimeListRepository
                .findByResultListId(resultList.getId())
                .isEmpty());
    }

    /**
     * Batch load hasSplitTimes for multiple events to avoid N+1 queries.
     */
    private Map<Long, Boolean> batchHasSplitTimes(List<Event> events) {
        return events.stream()
                .collect(
                        java.util.stream.Collectors.toMap(event -> event.getId().value(), this::hasSplitTimes));
    }

    @GetMapping("/event")
    public ResponseEntity<Page<EventDto>> searchEvents(
            @RequestParam Optional<String> filter, @Nullable Pageable pageable) {
        Page<Event> events = eventService.findAll(
                filter.orElse(null),
                pageable != null
                        ? FilterAndSortConverter.mapOrderProperties(pageable, EventDto::mapOrdersDtoToDomain)
                        : Pageable.unpaged());

        // Use batch loading to avoid N+1 queries when converting to DTOs
        Map<Long, Boolean> hasSplitTimesMap = batchHasSplitTimes(events.getContent());
        List<EventDto> eventDtos = eventMapper.toDtos(events.getContent(), hasSplitTimesMap);

        Map<EventId, Boolean> hasSplitTimesByEventId = hasSplitTimesByEventId(events.getContent());
        return ResponseEntity.ok(new PageImpl<>(
                eventDtos,
                events.getContent().stream()
                        .map(x -> EventDto.from(x, organisationService, eventCertificateService,
                                hasSplitTimesByEventId.getOrDefault(x.getId(), Boolean.FALSE)))
                        .map(x -> eventMapper.toDto(x, hasSplitTimes(x)))
                        .toList(),
                FilterAndSortConverter.mapOrderProperties(events.getPageable(), EventDto::mapOrdersDomainToDto),
                events.getTotalElements()));
    }

    private Map<EventId, Boolean> hasSplitTimesByEventId(Collection<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        Set<EventId> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        Map<EventId, Boolean> hasSplitTimesByEventId = new HashMap<>();
        eventIds.forEach(eventId -> hasSplitTimesByEventId.put(eventId, Boolean.FALSE));

        Map<ResultListId, EventId> resultListEventById = resultListService.findAll().stream()
                .filter(resultList -> eventIds.contains(resultList.getEventId()))
                .collect(Collectors.toMap(ResultList::getId, ResultList::getEventId, (left, right) -> left));

        if (resultListEventById.isEmpty()) {
            return hasSplitTimesByEventId;
        }

        splitTimeListRepository.findAll().stream()
                .map(SplitTimeList::getResultListId)
                .map(resultListEventById::get)
                .filter(Objects::nonNull)
                .forEach(eventId -> hasSplitTimesByEventId.put(eventId, Boolean.TRUE));

        return hasSplitTimesByEventId;
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
        Map<Long, Boolean> hasSplitTimesMap = batchHasSplitTimes(List.of(event));
        List<EventDto> eventDtos = eventMapper.toDtos(List.of(event), hasSplitTimesMap);
        return ResponseEntity.ok(eventDtos.get(0));
    }

    @GetMapping("/event/{id}")
    public ResponseEntity<EventDto> getEvent(@PathVariable Long id) {
        Event event = eventService.findById(EventId.of(id)).orElseThrow(ResourceNotFoundException::new);

        // Batch load organisations even for single event to avoid N+1
        Map<Long, Boolean> hasSplitTimesMap = batchHasSplitTimes(List.of(event));
        List<EventDto> eventDtos = eventMapper.toDtos(List.of(event), hasSplitTimesMap);

        return ResponseEntity.ok(eventDtos.get(0));
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
        Map<Long, Boolean> hasSplitTimesMap = batchHasSplitTimes(List.of(event));
        List<EventDto> eventDtos = eventMapper.toDtos(List.of(event), hasSplitTimesMap);
        return ResponseEntity.ok(eventDtos.get(0));
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
        return ResponseEntity.ok(eventResultsMapper.toDto(event, eventHasCup));
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
        CertificateServiceImpl.Certificate certificate = resultListService.createCertificate(event, eventCertificate);
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
