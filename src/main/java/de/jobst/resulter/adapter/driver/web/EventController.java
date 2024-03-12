package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.EventDto;
import de.jobst.resulter.adapter.driver.web.dto.EventResultsDto;
import de.jobst.resulter.adapter.driver.web.dto.EventStatusDto;
import de.jobst.resulter.application.EventService;
import de.jobst.resulter.application.ResultListService;
import de.jobst.resulter.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class EventController {

    private final EventService eventService;
    private final ResultListService resultListService;

    @Autowired
    public EventController(EventService eventService, ResultListService resultListService) {
        this.eventService = eventService;
        this.resultListService = resultListService;
    }

    private static void logError(Exception e) {
        log.error(e.getMessage());
        if (Objects.nonNull(e.getCause())) {
            log.error(e.getCause().getMessage());
        }
    }

    @GetMapping("/event")
    public ResponseEntity<Page<EventDto>> searchEvents(@RequestParam Optional<String> filter, Pageable pageable) {
        try {
            Page<Event> events = eventService.findAll(filter.orElse(null),
                pageable != null ?
                FilterAndSortConverter.mapOrderProperties(pageable, EventDto::mapOrdersDtoToDomain) :
                Pageable.unpaged());
            return ResponseEntity.ok(new PageImpl<>(events.getContent().stream().map(EventDto::from).toList(),
                FilterAndSortConverter.mapOrderProperties(events.getPageable(), EventDto::mapOrdersDomainToDto),
                events.getTotalElements()));
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/event_status")
    public ResponseEntity<List<EventStatusDto>> handleEventTypes() {
        try {
            List<EventStatusDto> eventStatus = Arrays.stream(EventStatus.values()).map(EventStatusDto::from).toList();
            return ResponseEntity.ok(eventStatus);
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/event")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventDto> createEvent(@RequestBody EventDto eventDto) {
        try {
            Event event = eventService.createEvent(eventDto.name(),
                ObjectUtils.isNotEmpty(eventDto.startTime()) ?
                ZonedDateTime.parse(eventDto.startTime(), DateTimeFormatter.ISO_DATE_TIME) :
                null,
                eventDto.organisations() == null ?
                new HashSet<>() :
                eventDto.organisations().stream().map(x -> OrganisationId.of(x.id())).collect(Collectors.toSet()));
            if (null != event) {
                return ResponseEntity.ok(EventDto.from(event));
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


    @GetMapping("/event/{id}")
    public ResponseEntity<EventDto> getEvent(@PathVariable Long id) {
        try {
            Optional<Event> event = eventService.findById(EventId.of(id));
            return event.map(value -> ResponseEntity.ok(EventDto.from(value)))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/event/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long id, @RequestBody EventDto eventDto) {
        try {
            Event event = eventService.updateEvent(EventId.of(id),
                EventName.of(eventDto.name()),
                ObjectUtils.isNotEmpty(eventDto.startTime()) ?
                DateTime.of(ZonedDateTime.parse(eventDto.startTime(), DateTimeFormatter.ISO_DATE_TIME)) :
                null,
                EventStatus.fromValue(eventDto.state().id()),
                eventDto.organisations() == null ?
                new HashSet<>() :
                eventDto.organisations().stream().map(x -> OrganisationId.of(x.id())).collect(Collectors.toSet()),
                eventDto.certificate() != null ? EventCertificateId.of(eventDto.certificate().id()) : null);

            if (null != event) {
                return ResponseEntity.ok(EventDto.from(event));
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

    @DeleteMapping("/event/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> deleteEvent(@PathVariable Long id) {
        try {
            boolean success = eventService.deleteEvent(EventId.of(id));
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

    @GetMapping("/event/{id}/results")
    public ResponseEntity<EventResultsDto> getEventResults(@PathVariable Long id) {
        try {
            Optional<Event> event = eventService.findById(EventId.of(id));

            return event.map(value -> ResponseEntity.ok(EventResultsDto.from(value, resultListService)))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/event/{id}/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventDto> calculateEvent(@PathVariable Long id) {
        try {
            Event event = null; //eventService.calculateEvent(EventId.of(id));
            if (null != event) {
                return ResponseEntity.ok(EventDto.from(event));
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
