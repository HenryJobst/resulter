package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.CupDto;
import de.jobst.resulter.application.CupService;
import de.jobst.resulter.application.EventService;
import de.jobst.resulter.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class CupController {

    private final CupService cupService;
    private final EventService eventService;

    @Autowired
    public CupController(CupService cupService, EventService eventService) {
        this.cupService = cupService;
        this.eventService = eventService;
    }

    @GetMapping("/cup")
    public ResponseEntity<List<CupDto>> handleCups(
            @RequestParam(name = "shallowEvents", required = false, defaultValue = "false")
            Boolean shallowEvents,
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
            @RequestParam(name = "shallowEventOrganisations", required = false, defaultValue = "true")
            Boolean shallowEventOrganisations
    ) {
        try {
            EventConfig eventConfig = EventService.getEventConfig(
                    shallowClassResults,
                    shallowPersonResults,
                    shallowPersonRaceResults,
                    shallowSplitTimes,
                    shallowPersons,
                    shallowOrganisations,
                    shallowEventOrganisations);
            CupConfig cupConfig = CupService.getCupConfig(shallowEvents, eventConfig);
            List<Cup> cups = cupService.findAll(cupConfig);
            return ResponseEntity.ok(cups.stream().map(CupDto::from).toList());
        } catch (Exception e) {
            log.error(e.getMessage());
            if (Objects.nonNull(e.getCause())) {
                log.error(e.getCause().getMessage());
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/cup/{id}")
    public ResponseEntity<CupDto> getCup(
            @PathVariable Long id,
            @RequestParam(name = "shallowEvents", required = false, defaultValue = "false")
            Boolean shallowEvents,
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
            @RequestParam(name = "shallowEventOrganisations", required = false, defaultValue = "true")
            Boolean shallowEventOrganisations
    ) {
        try {
            EventConfig eventConfig = EventService.getEventConfig(
                    shallowClassResults,
                    shallowPersonResults,
                    shallowPersonRaceResults,
                    shallowSplitTimes,
                    shallowPersons,
                    shallowOrganisations,
                    shallowEventOrganisations);
            CupConfig cupConfig = CupService.getCupConfig(shallowEvents, eventConfig);
            Optional<Cup> cup = cupService.findById(CupId.of(id), cupConfig);
            return cup.map(value -> ResponseEntity.ok(CupDto.from(value)))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            log.error(e.getMessage());
            if (Objects.nonNull(e.getCause())) {
                log.error(e.getCause().getMessage());
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/cup/{id}")
    public ResponseEntity<CupDto> updateCup(@PathVariable Long id, @RequestBody CupDto cupDto) {
        try {
            Cup cup = cupService.updateCup(
                    CupId.of(id),
                    CupName.of(cupDto.name()),
                    CupType.fromValue(cupDto.type()),
                    Events.of(
                            cupDto.events() == null ? new ArrayList<>() :
                                    cupDto.events().stream()
                                            .map(it -> eventService.findById(EventId.of(it.id()), EventConfig.empty())
                                                    .orElse(null))
                                            .filter(ObjectUtils::isNotEmpty)
                                            .toList()
                    )
            );
            if (null != cup) {
                return ResponseEntity.ok(CupDto.from(cup));
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
}
