package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.EventDto;
import de.jobst.resulter.application.EventService;
import de.jobst.resulter.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @GetMapping("/event")
    public ResponseEntity<List<EventDto>> handleEvents(
            @RequestParam(name = "shallowPersonRaceResults", required = false, defaultValue = "true")
            Boolean shallowPersonRaceResults,
            @RequestParam(name = "shallowSplitTimes", required = false, defaultValue = "true")
            Boolean shallowSplitTimes
    ) {
        try {
            EnumSet<EventConfig.ShallowLoads> shallowLoads = EnumSet.noneOf(EventConfig.ShallowLoads.class);
            if (shallowPersonRaceResults) {
                shallowLoads.add(EventConfig.ShallowLoads.PERSON_RACE_RESULTS);
                shallowLoads.add(EventConfig.ShallowLoads.SPLIT_TIMES);
            } else if (shallowSplitTimes) {
                shallowLoads.add(EventConfig.ShallowLoads.SPLIT_TIMES);
            }
            List<Event> events = eventService.findAll(EventConfig.of(shallowLoads));
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
    public ResponseEntity<EventDto> getEvent(@PathVariable Long id) {
        try {
            Optional<Event> event = eventService.findById(EventId.of(id));
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
    public ResponseEntity<EventDto> updateEvent(@PathVariable Long id, @RequestBody EventDto objekt) {
        try {
            Optional<Event> optionalEvent = eventService.findById(EventId.of(id));
            if (optionalEvent.isPresent()) {
                Event event = optionalEvent.get();
                event.setName(EventName.of(objekt.name()));
                if (ObjectUtils.isNotEmpty(objekt.startDate())) {
                    if (ObjectUtils.isNotEmpty(objekt.startTime())) {
                        event.setStartTime(DateTime.of(LocalDateTime.of(objekt.startDate(), objekt.startTime())));
                    } else {
                        event.setStartTime(DateTime.of(LocalDateTime.of(objekt.startDate(), LocalTime.of(11, 0))));
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
