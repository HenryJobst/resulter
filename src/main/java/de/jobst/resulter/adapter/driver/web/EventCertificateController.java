package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.constraints.CreateDtoGroup;
import de.jobst.resulter.adapter.driver.web.constraints.KeyDtoGroup;
import de.jobst.resulter.adapter.driver.web.constraints.ValidId;
import de.jobst.resulter.adapter.driver.web.dto.EventCertificateDto;
import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.MediaFileService;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.EventCertificateLayoutDescription;
import de.jobst.resulter.domain.EventCertificateName;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Validated
public class EventCertificateController {

    private final EventCertificateService eventCertificateService;
    private final EventService eventService;
    private final MediaFileService mediaFileService;

    @Value("#{'${resulter.media-file-path-thumbnails}'}")
    private String mediaFileThumbnailsPath;

    @Autowired
    public EventCertificateController(
            EventCertificateService eventCertificateService,
            EventService eventService,
            MediaFileService mediaFileService) {
        this.eventCertificateService = eventCertificateService;
        this.eventService = eventService;
        this.mediaFileService = mediaFileService;
    }

    @GetMapping("/event_certificate/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EventCertificateDto>> getAllEventCertificates() {
        List<EventCertificate> eventCertificates = eventCertificateService.findAll();
        return ResponseEntity.ok(eventCertificates.stream()
                .map(x -> EventCertificateDto.from(x, mediaFileThumbnailsPath, eventService, mediaFileService))
                .toList());
    }

    @GetMapping("/event_certificate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<EventCertificateDto>> searchEventCertificates(
            @RequestParam Optional<String> filter, Pageable pageable) {
        Page<EventCertificate> eventCertificates = eventCertificateService.findAll(
                filter.orElse(null),
                pageable != null
                        ? FilterAndSortConverter.mapOrderProperties(pageable, EventCertificateDto::mapOrdersDtoToDomain)
                        : Pageable.unpaged());
        return ResponseEntity.ok(new PageImpl<>(
                eventCertificates.getContent().stream()
                        .map(x -> EventCertificateDto.from(x, mediaFileThumbnailsPath, eventService, mediaFileService))
                        .toList(),
                FilterAndSortConverter.mapOrderProperties(
                        eventCertificates.getPageable(), EventCertificateDto::mapOrdersDomainToDto),
                eventCertificates.getTotalElements()));
    }

    @PostMapping("/event_certificate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventCertificateDto> createEventCertificate(
            @Validated(CreateDtoGroup.class) @RequestBody EventCertificateDto eventCertificateDto) {
        EventCertificate eventCertificate = eventCertificateService.createEventCertificate(
                eventCertificateDto.name(),
                eventCertificateDto.event(),
                eventCertificateDto.layoutDescription(),
                eventCertificateDto.blankCertificate(),
                eventCertificateDto.primary());
        return ResponseEntity.ok(
                EventCertificateDto.from(eventCertificate, mediaFileThumbnailsPath, eventService, mediaFileService));
    }

    @GetMapping("/event_certificate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventCertificateDto> getEventCertificate(@PathVariable Long id) {
        Optional<EventCertificate> eventCertificate = eventCertificateService.findById(EventCertificateId.of(id));
        return eventCertificate
                .map(value -> ResponseEntity.ok(
                        EventCertificateDto.from(value, mediaFileThumbnailsPath, eventService, mediaFileService)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/event_certificate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventCertificateDto> updateEventCertificate(
            @ValidId @PathVariable Long id,
            @RequestBody @Validated(KeyDtoGroup.class) EventCertificateDto eventCertificateDto) {
        EventCertificate eventCertificate = eventCertificateService.updateEventCertificate(
                EventCertificateId.of(id),
                EventCertificateName.of(eventCertificateDto.name()),
                eventCertificateDto.event(),
                EventCertificateLayoutDescription.of(eventCertificateDto.layoutDescription()),
                eventCertificateDto.blankCertificate(),
                eventCertificateDto.primary());

        return ResponseEntity.ok(
                EventCertificateDto.from(eventCertificate, mediaFileThumbnailsPath, eventService, mediaFileService));
    }

    @DeleteMapping("/event_certificate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEventCertificate(@ValidId @PathVariable Long id) {
        eventCertificateService.deleteEventCertificate(EventCertificateId.of(id));
        return ResponseEntity.noContent().build();
    }
}
