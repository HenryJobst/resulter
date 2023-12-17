package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.EventDto;
import de.jobst.resulter.application.EventService;
import de.jobst.resulter.domain.DateTime;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.EventName;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
    public ResponseEntity<List<EventDto>> handleEvents() {
        try {
            List<Event> events = eventService.findAll();
            return ResponseEntity.ok(events.stream().map(it -> EventDto.from(it)).toList());
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
            if (event.isPresent()) {
                return ResponseEntity.ok(EventDto.from(event.get()));
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
