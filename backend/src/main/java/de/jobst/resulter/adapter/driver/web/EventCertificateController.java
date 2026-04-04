package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.constraints.CreateDtoGroup;
import de.jobst.resulter.adapter.driver.web.constraints.KeyDtoGroup;
import de.jobst.resulter.adapter.driver.web.constraints.ValidId;
import de.jobst.resulter.adapter.driver.web.dto.EventCertificateDto;
import de.jobst.resulter.adapter.driver.web.mapper.EventCertificateMapper;
import de.jobst.resulter.application.port.EventCertificateQueryService;
import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.util.FilterAndSortConverter;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.EventCertificateLayoutDescription;
import de.jobst.resulter.domain.EventCertificateName;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.MediaFileId;
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
    private final EventCertificateQueryService eventCertificateQueryService;

    @Value("#{'${resulter.media-file-path-thumbnails}'}")
    private String mediaFileThumbnailsPath;

    public EventCertificateController(
            EventCertificateService eventCertificateService,
            EventCertificateQueryService eventCertificateQueryService) {
        this.eventCertificateService = eventCertificateService;
        this.eventCertificateQueryService = eventCertificateQueryService;
    }

    @GetMapping("/event_certificate/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EventCertificateDto>> getAllEventCertificates() {
        var result = eventCertificateQueryService.findAll();
        return ResponseEntity.ok(EventCertificateMapper.toDtos(
                result.eventCertificates(), result.eventMap(), result.mediaFileMap(), mediaFileThumbnailsPath));
    }

    @GetMapping("/event_certificate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<EventCertificateDto>> searchEventCertificates(
            @RequestParam Optional<String> filter, @Nullable Pageable pageable) {
        var result = eventCertificateQueryService.findAll(
                filter.orElse(null),
                pageable != null
                        ? FilterAndSortConverter.mapOrderProperties(pageable, EventCertificateDto::mapOrdersDtoToDomain)
                        : Pageable.unpaged());
        return ResponseEntity.ok(new PageImpl<>(
                EventCertificateMapper.toDtos(
                        result.eventCertificates(), result.eventMap(), result.mediaFileMap(), mediaFileThumbnailsPath),
                FilterAndSortConverter.mapOrderProperties(
                        result.resolvedPageable(), EventCertificateDto::mapOrdersDomainToDto),
                result.totalElements()));
    }

    @PostMapping("/event_certificate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventCertificateDto> createEventCertificate(
            @Validated(CreateDtoGroup.class) @RequestBody EventCertificateDto eventCertificateDto) {
        var eventCertificate = eventCertificateService.createEventCertificate(
                eventCertificateDto.name(),
                EventId.of(eventCertificateDto.event().id()),
                eventCertificateDto.layoutDescription(),
                MediaFileId.of(eventCertificateDto.blankCertificate().id()),
                eventCertificateDto.primary());
        return eventCertificateQueryService
                .findById(eventCertificate.getId().value())
                .map(result -> EventCertificateMapper.toDtos(
                                result.eventCertificates(),
                                result.eventMap(),
                                result.mediaFileMap(),
                                mediaFileThumbnailsPath)
                        .get(0))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/event_certificate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventCertificateDto> getEventCertificate(@PathVariable Long id) {
        return eventCertificateQueryService
                .findById(id)
                .map(result -> EventCertificateMapper.toDtos(
                                result.eventCertificates(),
                                result.eventMap(),
                                result.mediaFileMap(),
                                mediaFileThumbnailsPath)
                        .get(0))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/event_certificate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventCertificateDto> updateEventCertificate(
            @ValidId @PathVariable Long id,
            @RequestBody @Validated(KeyDtoGroup.class) EventCertificateDto eventCertificateDto) {
        eventCertificateService.updateEventCertificate(
                EventCertificateId.of(id),
                EventCertificateName.of(eventCertificateDto.name()),
                EventId.of(eventCertificateDto.event().id()),
                EventCertificateLayoutDescription.of(eventCertificateDto.layoutDescription()),
                MediaFileId.of(eventCertificateDto.blankCertificate().id()),
                eventCertificateDto.primary());
        return eventCertificateQueryService
                .findById(id)
                .map(result -> EventCertificateMapper.toDtos(
                                result.eventCertificates(),
                                result.eventMap(),
                                result.mediaFileMap(),
                                mediaFileThumbnailsPath)
                        .get(0))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/event_certificate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEventCertificate(@ValidId @PathVariable Long id) {
        eventCertificateService.deleteEventCertificate(EventCertificateId.of(id));
        return ResponseEntity.noContent().build();
    }
}
