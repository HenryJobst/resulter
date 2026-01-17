package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.constraints.CreateDtoGroup;
import de.jobst.resulter.adapter.driver.web.constraints.KeyDtoGroup;
import de.jobst.resulter.adapter.driver.web.constraints.ValidId;
import de.jobst.resulter.adapter.driver.web.dto.EventCertificateDto;
import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.MediaFileService;
import de.jobst.resulter.application.util.FilterAndSortConverter;
import de.jobst.resulter.domain.*;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
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
    private final de.jobst.resulter.adapter.driver.web.mapper.EventCertificateMapper eventCertificateMapper;

    @Value("#{'${resulter.media-file-path-thumbnails}'}")
    private String mediaFileThumbnailsPath;

    public EventCertificateController(
            EventCertificateService eventCertificateService,
            EventService eventService,
            MediaFileService mediaFileService,
            de.jobst.resulter.adapter.driver.web.mapper.EventCertificateMapper eventCertificateMapper) {
        this.eventCertificateService = eventCertificateService;
        this.eventService = eventService;
        this.mediaFileService = mediaFileService;
        this.eventCertificateMapper = eventCertificateMapper;
    }

    @GetMapping("/event_certificate/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EventCertificateDto>> getAllEventCertificates() {
        List<EventCertificate> eventCertificates = eventCertificateService.findAll();
        return ResponseEntity.ok(eventCertificates.stream()
                .map(x -> eventCertificateMapper.toDto(x, mediaFileThumbnailsPath))
                .toList());
    }

    @GetMapping("/event_certificate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<EventCertificateDto>> searchEventCertificates(
            @RequestParam Optional<String> filter, @Nullable Pageable pageable) {
        Page<EventCertificate> eventCertificates = eventCertificateService.findAll(
                filter.orElse(null),
                pageable != null
                        ? FilterAndSortConverter.mapOrderProperties(pageable, EventCertificateDto::mapOrdersDtoToDomain)
                        : Pageable.unpaged());
        return ResponseEntity.ok(new PageImpl<>(
                eventCertificates.getContent().stream()
                        .map(x -> eventCertificateMapper.toDto(x, mediaFileThumbnailsPath))
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
                EventId.of(eventCertificateDto.event().id()),
                eventCertificateDto.layoutDescription(),
                MediaFileId.of(eventCertificateDto.blankCertificate().id()),
                eventCertificateDto.primary());
        return ResponseEntity.ok(eventCertificateMapper.toDto(eventCertificate, mediaFileThumbnailsPath));
    }

    @GetMapping("/event_certificate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventCertificateDto> getEventCertificate(@PathVariable Long id) {
        Optional<EventCertificate> eventCertificate = eventCertificateService.findById(EventCertificateId.of(id));
        return eventCertificate
                .map(value -> ResponseEntity.ok(eventCertificateMapper.toDto(value, mediaFileThumbnailsPath)))
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
                EventId.of(eventCertificateDto.event().id()),
                EventCertificateLayoutDescription.of(eventCertificateDto.layoutDescription()),
                MediaFileId.of(eventCertificateDto.blankCertificate().id()),
                eventCertificateDto.primary());

        return ResponseEntity.ok(eventCertificateMapper.toDto(eventCertificate, mediaFileThumbnailsPath));
    }

    @DeleteMapping("/event_certificate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEventCertificate(@ValidId @PathVariable Long id) {
        eventCertificateService.deleteEventCertificate(EventCertificateId.of(id));
        return ResponseEntity.noContent().build();
    }
}
