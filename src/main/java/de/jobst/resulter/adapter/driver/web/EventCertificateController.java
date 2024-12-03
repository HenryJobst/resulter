package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.EventCertificateDto;
import de.jobst.resulter.application.EventCertificateService;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.EventCertificateLayoutDescription;
import de.jobst.resulter.domain.EventCertificateName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

@RestController
@Slf4j
public class EventCertificateController {

    private final EventCertificateService eventCertificateService;

    @Value("#{'${resulter.media-file-path-thumbnails}'}")
    private String mediaFileThumbnailsPath;

    @Autowired
    public EventCertificateController(EventCertificateService eventCertificateService) {
        this.eventCertificateService = eventCertificateService;
    }

    private static void logError(Exception e) {
        log.error(e.getMessage());
        if (Objects.nonNull(e.getCause())) {
            log.error(e.getCause().getMessage());
        }
    }

    @GetMapping("/event_certificate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<EventCertificateDto>> searchEventCertificates(@RequestParam Optional<String> filter,
                                                                             Pageable pageable) {
        try {
            Page<EventCertificate> eventCertificates = eventCertificateService.findAll(filter.orElse(null),
                pageable != null ?
                FilterAndSortConverter.mapOrderProperties(pageable, EventCertificateDto::mapOrdersDtoToDomain) :
                Pageable.unpaged());
            return ResponseEntity.ok(new PageImpl<>(eventCertificates.getContent()
                .stream()
                .map(x -> EventCertificateDto.from(x, mediaFileThumbnailsPath))
                .toList(),
                FilterAndSortConverter.mapOrderProperties(eventCertificates.getPageable(),
                    EventCertificateDto::mapOrdersDomainToDto),
                eventCertificates.getTotalElements()));
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/event_certificate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventCertificateDto> createEventCertificate(
        @RequestBody EventCertificateDto eventCertificateDto) {
        try {
            EventCertificate eventCertificate =
                eventCertificateService.createEventCertificate(eventCertificateDto.name(),
                    eventCertificateDto.event(),
                    eventCertificateDto.layoutDescription(),
                    eventCertificateDto.blankCertificate(),
                    eventCertificateDto.primary());

            if (null != eventCertificate) {
                return ResponseEntity.ok(EventCertificateDto.from(eventCertificate, mediaFileThumbnailsPath));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/event_certificate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventCertificateDto> getEventCertificate(@PathVariable Long id) {
        try {
            Optional<EventCertificate> eventCertificate = eventCertificateService.findById(EventCertificateId.of(id));
            return eventCertificate.map(value -> ResponseEntity.ok(EventCertificateDto.from(value,
                mediaFileThumbnailsPath))).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/event_certificate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventCertificateDto> updateEventCertificate(@PathVariable Long id,
                                                                      @RequestBody
                                                                      EventCertificateDto eventCertificateDto) {
        try {
            EventCertificate eventCertificate =
                eventCertificateService.updateEventCertificate(EventCertificateId.of(id),
                    EventCertificateName.of(eventCertificateDto.name()),
                    eventCertificateDto.event(),
                    EventCertificateLayoutDescription.of(eventCertificateDto.layoutDescription()),
                    eventCertificateDto.blankCertificate(),
                    eventCertificateDto.primary());

            if (null != eventCertificate) {
                return ResponseEntity.ok(EventCertificateDto.from(eventCertificate, mediaFileThumbnailsPath));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/event_certificate/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> deleteEventCertificate(@PathVariable Long id) {
        try {
            boolean success = eventCertificateService.deleteEventCertificate(EventCertificateId.of(id));
            if (success) {
                return ResponseEntity.ok(Boolean.TRUE);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            logError(e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logError(e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
