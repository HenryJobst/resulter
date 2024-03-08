package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.EventCertificateDto;
import de.jobst.resulter.application.EventCertificateService;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.EventCertificateLayoutDescription;
import de.jobst.resulter.domain.EventCertificateName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

@RestController
@Slf4j
public class EventCertificateController {

    private final EventCertificateService eventCertificateService;

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
    public ResponseEntity<Page<EventCertificateDto>> searchEventCertificates(@RequestParam Optional<String> filter,
                                                                             Pageable pageable) {
        try {
            Page<EventCertificate> eventCertificates = eventCertificateService.findAll(filter.orElse(null),
                pageable != null ?
                FilterAndSortConverter.mapOrderProperties(pageable, EventCertificateDto::mapOrdersDtoToDomain) :
                Pageable.unpaged());
            return ResponseEntity.ok(new PageImpl<>(eventCertificates.getContent()
                .stream()
                .map(EventCertificateDto::from)
                .toList(),
                FilterAndSortConverter.mapOrderProperties(eventCertificates.getPageable(),
                    EventCertificateDto::mapOrdersDomainToDto),
                eventCertificates.getTotalElements()));
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
                    eventCertificateDto.blankCertificate());

            if (null != eventCertificate) {
                return ResponseEntity.ok(EventCertificateDto.from(eventCertificate));
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/event_certificate/{id}")
    public ResponseEntity<EventCertificateDto> getEventCertificate(@PathVariable Long id) {
        try {
            Optional<EventCertificate> eventCertificate = eventCertificateService.findById(EventCertificateId.of(id));
            return eventCertificate.map(value -> ResponseEntity.ok(EventCertificateDto.from(value)))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
                    eventCertificateDto.blankCertificate());

            if (null != eventCertificate) {
                return ResponseEntity.ok(EventCertificateDto.from(eventCertificate));
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
