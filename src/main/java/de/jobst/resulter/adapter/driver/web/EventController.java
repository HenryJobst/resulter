package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.EventDto;
import de.jobst.resulter.application.EventService;
import de.jobst.resulter.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @NonNull
    private static EventConfig getEventConfig(Boolean shallowClassResults,
                                              Boolean shallowPersonResults,
                                              Boolean shallowPersonRaceResults,
                                              Boolean shallowSplitTimes,
                                              Boolean shallowPersons,
                                              Boolean shallowOrganisations,
                                              Boolean shallowEventOrganisations) {
        EnumSet<EventConfig.ShallowLoads> shallowLoads = EnumSet.noneOf(EventConfig.ShallowLoads.class);
        if (shallowEventOrganisations) {
            shallowLoads.add(EventConfig.ShallowLoads.EVENT_ORGANISATIONS);
        }
        if (shallowPersons) {
            shallowLoads.add(EventConfig.ShallowLoads.PERSONS);
        }
        if (shallowOrganisations) {
            shallowLoads.add(EventConfig.ShallowLoads.ORGANISATIONS);
        }
        if (shallowClassResults) {
            shallowLoads.add(EventConfig.ShallowLoads.CLASS_RESULTS);
            shallowLoads.add(EventConfig.ShallowLoads.PERSON_RESULTS);
            shallowLoads.add(EventConfig.ShallowLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(EventConfig.ShallowLoads.SPLIT_TIMES);
            shallowLoads.add(EventConfig.ShallowLoads.PERSONS);
            shallowLoads.add(EventConfig.ShallowLoads.ORGANISATIONS);
        } else if (shallowPersonResults) {
            shallowLoads.add(EventConfig.ShallowLoads.PERSON_RESULTS);
            shallowLoads.add(EventConfig.ShallowLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(EventConfig.ShallowLoads.SPLIT_TIMES);
            shallowLoads.add(EventConfig.ShallowLoads.PERSONS);
            shallowLoads.add(EventConfig.ShallowLoads.ORGANISATIONS);
        } else if (shallowPersonRaceResults) {
            shallowLoads.add(EventConfig.ShallowLoads.PERSON_RACE_RESULTS);
            shallowLoads.add(EventConfig.ShallowLoads.SPLIT_TIMES);
        } else if (shallowSplitTimes) {
            shallowLoads.add(EventConfig.ShallowLoads.SPLIT_TIMES);
        }
        return EventConfig.of(shallowLoads);
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
            Boolean shallowEventOrganisations
    ) {
        try {
            EventConfig
                    eventConfig =
                    getEventConfig(shallowClassResults,
                            shallowPersonResults,
                            shallowPersonRaceResults,
                            shallowSplitTimes,
                            shallowPersons,
                            shallowOrganisations,
                            shallowEventOrganisations);
            List<Event> events = eventService.findAll(eventConfig);
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
            Boolean shallowEventOrganisations
    ) {
        try {
            EventConfig
                    eventConfig =
                    getEventConfig(shallowClassResults,
                            shallowPersonResults,
                            shallowPersonRaceResults,
                            shallowSplitTimes,
                            shallowPersons,
                            shallowOrganisations,
                            shallowEventOrganisations);
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
            EventConfig
                    eventConfig =
                    getEventConfig(true,
                            true,
                            true,
                            true,
                            true,
                            true,
                            false);
            Optional<Event> optionalEvent = eventService.findById(EventId.of(id), eventConfig);
            if (optionalEvent.isPresent()) {
                Event event = optionalEvent.get();
                event.setName(EventName.of(eventDto.name()));
                if (ObjectUtils.isNotEmpty(eventDto.startTime())) {
                    if (ObjectUtils.isNotEmpty(eventDto.startTime())) {
                        event.setStartTime(DateTime.of(ZonedDateTime.parse(eventDto.startTime(),
                                DateTimeFormatter.ISO_DATE_TIME)));
                    }
                } else {
                    event.setStartTime(null);
                }
                eventService.updateEvent(event);
                return ResponseEntity.ok(EventDto.from(event));
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(e.getMessage());
            if (Objects.nonNull(e.getCause())) {
                log.error(e.getCause().getMessage());
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
