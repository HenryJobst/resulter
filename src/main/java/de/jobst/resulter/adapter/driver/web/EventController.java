package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.EventDto;
import de.jobst.resulter.adapter.driver.web.dto.EventResultsDto;
import de.jobst.resulter.application.CupService;
import de.jobst.resulter.application.EventService;
import de.jobst.resulter.application.OrganisationService;
import de.jobst.resulter.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class EventController {

    private final EventService eventService;
    private final OrganisationService organisationService;
    private final CupService cupService;

    @Autowired
    public EventController(EventService eventService, OrganisationService organisationService, CupService cupService) {
        this.eventService = eventService;
        this.organisationService = organisationService;
        this.cupService = cupService;
    }

    @GetMapping("/event")
    public ResponseEntity<List<EventDto>> handleEvents(
            @RequestParam(name = "shallowClassResults", required = false, defaultValue = "false")
            Boolean shallowClassResults,
            @RequestParam(name = "shallowPersonResults", required = false, defaultValue = "false")
            Boolean shallowPersonResults,
            @RequestParam(name = "shallowPersonRaceResults", required = false, defaultValue = "true")
            Boolean shallowPersonRaceResults,
            @RequestParam(name = "shallowSplitTimes", required = false, defaultValue = "true")
            Boolean shallowSplitTimes,
            @RequestParam(name = "shallowPersons", required = false, defaultValue = "true")
            Boolean shallowPersons,
            @RequestParam(name = "shallowOrganisations", required = false, defaultValue = "true")
            Boolean shallowOrganisations,
            @RequestParam(name = "shallowEventOrganisations", required = false, defaultValue = "false")
            Boolean shallowEventOrganisations,
            @RequestParam(name = "shallowCups", required = false, defaultValue = "true")
            Boolean shallowCups
    ) {
        try {
            List<Event> events = eventService.findAll(EventService.getEventConfig(shallowClassResults,
                    shallowPersonResults,
                    shallowPersonRaceResults,
                    shallowSplitTimes,
                    shallowPersons,
                    shallowOrganisations,
                    shallowEventOrganisations,
                    shallowCups));
            return ResponseEntity.ok(events.stream().map(EventDto::from).toList());
        } catch (Exception e) {
            log.error(e.getMessage());
            if (Objects.nonNull(e.getCause())) {
                log.error(e.getCause().getMessage());
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/event/{id}")
    public ResponseEntity<EventDto> getEvent(
            @PathVariable Long id,
            @RequestParam(name = "shallowClassResults", required = false, defaultValue = "true")
            Boolean shallowClassResults,
            @RequestParam(name = "shallowPersonResults", required = false, defaultValue = "true")
            Boolean shallowPersonResults,
            @RequestParam(name = "shallowPersonRaceResults", required = false, defaultValue = "true")
            Boolean shallowPersonRaceResults,
            @RequestParam(name = "shallowSplitTimes", required = false, defaultValue = "true")
            Boolean shallowSplitTimes,
            @RequestParam(name = "shallowPersons", required = false, defaultValue = "true")
            Boolean shallowPersons,
            @RequestParam(name = "shallowOrganisations", required = false, defaultValue = "true")
            Boolean shallowOrganisations,
            @RequestParam(name = "shallowEventOrganisations", required = false, defaultValue = "false")
            Boolean shallowEventOrganisations,
            @RequestParam(name = "shallowCups", required = false, defaultValue = "true")
            Boolean shallowCups
    ) {
        try {
            EventConfig eventConfig = EventService.getEventConfig(shallowClassResults,
                    shallowPersonResults,
                    shallowPersonRaceResults,
                    shallowSplitTimes,
                    shallowPersons,
                    shallowOrganisations,
                    shallowEventOrganisations,
                    shallowCups);
            Optional<Event> event = eventService.findById(EventId.of(id), eventConfig);
            return event.map(value -> ResponseEntity.ok(EventDto.from(value)))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.error(e.getMessage());
            if (Objects.nonNull(e.getCause())) {
                log.error(e.getCause().getMessage());
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/event/{id}")
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long id, @RequestBody EventDto eventDto) {
        try {
            Event event = eventService.updateEvent(
                    EventId.of(id),
                    EventName.of(eventDto.name()),
                    ObjectUtils.isNotEmpty(eventDto.startTime()) ?
                            DateTime.of(ZonedDateTime.parse(eventDto.startTime(),
                                    DateTimeFormatter.ISO_DATE_TIME)) : null,
                    Organisations.of(
                            eventDto.organisations() == null ? new ArrayList<>() :
                                    Arrays.stream(eventDto.organisations())
                                            .map(it -> organisationService.findById(OrganisationId.of(it)).orElse(null))
                                            .filter(ObjectUtils::isNotEmpty)
                                            .toList()
                    ),
                    Cups.of(
                            eventDto.cups() == null ? new ArrayList<>() :
                                    Arrays.stream(eventDto.cups())
                                            .map(it -> cupService.findById(CupId.of(it), CupConfig.full())
                                                    .orElse(null))
                                            .filter(ObjectUtils::isNotEmpty)
                                            .toList()
                    )
            );
            if (null != event) {
                return ResponseEntity.ok(EventDto.from(event));
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            if (Objects.nonNull(e.getCause())) {
                log.error(e.getCause().getMessage());
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error(e.getMessage());
            if (Objects.nonNull(e.getCause())) {
                log.error(e.getCause().getMessage());
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/event/{id}")
    public ResponseEntity<Boolean> deleteEvent(@PathVariable Long id) {
        try {
            boolean success = eventService.deleteEvent(EventId.of(id));
            if (success) {
                return ResponseEntity.ok(Boolean.TRUE);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            if (Objects.nonNull(e.getCause())) {
                log.error(e.getCause().getMessage());
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error(e.getMessage());
            if (Objects.nonNull(e.getCause())) {
                log.error(e.getCause().getMessage());
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/event/{id}/results")
    public ResponseEntity<EventResultsDto> getEventResults(
            @PathVariable Long id,
            @RequestParam(name = "shallowClassResults", required = false, defaultValue = "false")
            Boolean shallowClassResults,
            @RequestParam(name = "shallowPersonResults", required = false, defaultValue = "false")
            Boolean shallowPersonResults,
            @RequestParam(name = "shallowPersonRaceResults", required = false, defaultValue = "false")
            Boolean shallowPersonRaceResults,
            @RequestParam(name = "shallowSplitTimes", required = false, defaultValue = "true")
            Boolean shallowSplitTimes,
            @RequestParam(name = "shallowPersons", required = false, defaultValue = "false")
            Boolean shallowPersons,
            @RequestParam(name = "shallowOrganisations", required = false, defaultValue = "false")
            Boolean shallowOrganisations,
            @RequestParam(name = "shallowEventOrganisations", required = false, defaultValue = "true")
            Boolean shallowEventOrganisations,
            @RequestParam(name = "shallowCups", required = false, defaultValue = "true")
            Boolean shallowCups
    ) {
        try {
            EventConfig eventConfig = EventService.getEventConfig(shallowClassResults,
                    shallowPersonResults,
                    shallowPersonRaceResults,
                    shallowSplitTimes,
                    shallowPersons,
                    shallowOrganisations,
                    shallowEventOrganisations,
                    shallowCups);
            Optional<Event> event = eventService.findById(EventId.of(id), eventConfig);

            return event.map(value -> ResponseEntity.ok(EventResultsDto.from(value)))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

        } catch (Exception e) {
            log.error(e.getMessage());
            if (Objects.nonNull(e.getCause())) {
                log.error(e.getCause().getMessage());
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
