package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.EventCertificateDto;
import de.jobst.resulter.adapter.driver.web.dto.EventDto;
import de.jobst.resulter.adapter.driver.web.dto.EventResultsDto;
import de.jobst.resulter.adapter.driver.web.dto.EventStatusDto;
import de.jobst.resulter.application.EventService;
import de.jobst.resulter.application.RaceService;
import de.jobst.resulter.application.ResultListService;
import de.jobst.resulter.application.certificate.CertificateService;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import de.jobst.resulter.domain.util.ResponseNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Slf4j
@Validated
public class EventController {

    private final EventService eventService;
    private final ResultListService resultListService;
    private final RaceService raceService;

    @Autowired
    public EventController(EventService eventService, ResultListService resultListService, RaceService raceService) {
        this.eventService = eventService;
        this.resultListService = resultListService;
        this.raceService = raceService;
    }

    @GetMapping("/event/all")
    public ResponseEntity<List<EventDto>> getAllEvents() {
        List<Event> events = eventService.findAll();
        return ResponseEntity.ok(events.stream().map(EventDto::from).toList());
    }

    @GetMapping("/event")
    public ResponseEntity<Page<EventDto>> searchEvents(@RequestParam Optional<String> filter, Pageable pageable) {
        Page<Event> events = eventService.findAll(
                filter.orElse(null),
                pageable != null
                        ? FilterAndSortConverter.mapOrderProperties(pageable, EventDto::mapOrdersDtoToDomain)
                        : Pageable.unpaged());
        return ResponseEntity.ok(new PageImpl<>(
                events.getContent().stream().map(EventDto::from).toList(),
                FilterAndSortConverter.mapOrderProperties(events.getPageable(), EventDto::mapOrdersDomainToDto),
                events.getTotalElements()));
    }

    @GetMapping("/event_status")
    public ResponseEntity<List<EventStatusDto>> handleEventTypes() {
        List<EventStatusDto> eventStatus =
                Arrays.stream(EventStatus.values()).map(EventStatusDto::from).toList();
        return ResponseEntity.ok(eventStatus);
    }

    @PostMapping("/event")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventDto> createEvent(@RequestBody EventDto eventDto) {
        Event event = eventService.createEvent(
                eventDto.name(),
                ObjectUtils.isNotEmpty(eventDto.startTime())
                        ? ZonedDateTime.parse(eventDto.startTime(), DateTimeFormatter.ISO_DATE_TIME)
                        : null,
                eventDto.organisations() == null
                        ? new HashSet<>()
                        : eventDto.organisations().stream()
                                .map(x -> OrganisationId.of(x.id()))
                                .collect(Collectors.toSet()));
        if (null == event) {
            throw new ResponseNotFoundException("Event could not be created");
        }
        return ResponseEntity.ok(EventDto.from(event));
    }

    @GetMapping("/event/{id}")
    public ResponseEntity<EventDto> getEvent(@PathVariable Long id) {
        Event event = eventService.findById(EventId.of(id)).orElseThrow(ResourceNotFoundException::new);
        return ResponseEntity.ok(EventDto.from(event));
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
                        : null);
        return ResponseEntity.ok(EventDto.from(event));
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
        return ResponseEntity.ok(EventResultsDto.from(event, resultListService, raceService));
    }

    @PutMapping("/event/{id}/certificate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ByteArrayResource> getCertificate(
            @PathVariable Long id, @Valid @RequestBody EventCertificateDto eventCertificateDto) {
        CertificateService.Certificate certificate =
                resultListService.createCertificate(EventId.of(id), eventCertificateDto);
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
