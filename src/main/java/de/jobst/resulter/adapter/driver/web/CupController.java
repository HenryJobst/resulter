package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.CupDto;
import de.jobst.resulter.adapter.driver.web.dto.CupTypeDto;
import de.jobst.resulter.application.CupService;
import de.jobst.resulter.application.EventService;
import de.jobst.resulter.application.EventShallowProxyConfig;
import de.jobst.resulter.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class CupController {

    private final CupService cupService;

    @Autowired
    public CupController(CupService cupService) {
        this.cupService = cupService;
    }

    private static void logError(Exception e) {
        log.error(e.getMessage());
        if (Objects.nonNull(e.getCause())) {
            log.error(e.getCause().getMessage());
        }
    }

    @GetMapping("/cup_types")
    public ResponseEntity<List<CupTypeDto>> handleCupTypes() {
        try {
            List<CupTypeDto> cupTypes = Arrays.stream(CupType.values()).map(CupTypeDto::from).toList();
            return ResponseEntity.ok(cupTypes);
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/cup")
    public ResponseEntity<List<CupDto>> handleCups(
        @RequestParam(name = "shallowEvents", required = false, defaultValue = "false") Boolean shallowEvents,
        @RequestParam(name = "shallowClassResults", required = false, defaultValue = "false")
        Boolean shallowClassResults,
        @RequestParam(name = "shallowPersonResults", required = false, defaultValue = "false")
        Boolean shallowPersonResults,
        @RequestParam(name = "shallowPersonRaceResults", required = false, defaultValue = "true")
        Boolean shallowPersonRaceResults,
        @RequestParam(name = "shallowSplitTimes", required = false, defaultValue = "true") Boolean shallowSplitTimes,
        @RequestParam(name = "shallowCupScores", required = false, defaultValue = "true") Boolean shallowCupScores,
        @RequestParam(name = "shallowPersons", required = false, defaultValue = "true") Boolean shallowPersons,
        @RequestParam(name = "shallowOrganisations", required = false, defaultValue = "true")
        Boolean shallowOrganisations,
        @RequestParam(name = "shallowEventOrganisations", required = false, defaultValue = "true")
        Boolean shallowEventOrganisations) {
        try {
            EventConfig eventConfig = EventService.getEventConfig(new EventShallowProxyConfig(shallowClassResults,
                shallowPersonResults,
                shallowPersonRaceResults,
                shallowSplitTimes,
                shallowCupScores,
                shallowPersons,
                shallowOrganisations,
                shallowEventOrganisations));
            List<Cup> cups = cupService.findAll();
            return ResponseEntity.ok(cups.stream().map(CupDto::from).toList());
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/cup/{id}")
    public ResponseEntity<CupDto> getCup(@PathVariable Long id) {
        try {
            Optional<Cup> cup = cupService.findById(CupId.of(id));
            return cup.map(value -> ResponseEntity.ok(CupDto.from(value)))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/cup/{id}")
    public ResponseEntity<CupDto> updateCup(@PathVariable Long id, @RequestBody CupDto cupDto) {
        try {
            Cup cup = cupService.updateCup(CupId.of(id),
                CupName.of(cupDto.name()),
                CupType.fromValue(cupDto.type().id()),
                cupDto.eventIds() == null ? new ArrayList<>() : cupDto.eventIds().stream().map(EventId::of).toList());
            if (null != cup) {
                return ResponseEntity.ok(CupDto.from(cup));
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (DataIntegrityViolationException e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (IllegalArgumentException e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/cup")
    public ResponseEntity<CupDto> createCup(@RequestBody CupDto cupDto) {
        try {
            Cup cup = cupService.createCup(cupDto.name(),
                CupType.fromValue(cupDto.type().id()),
                cupDto.eventIds() == null ?
                new ArrayList<>() :
                cupDto.eventIds().stream().map(EventId::of).filter(ObjectUtils::isNotEmpty).toList());
            if (null != cup) {
                return ResponseEntity.ok(CupDto.from(cup));
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (DataIntegrityViolationException e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (IllegalArgumentException e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/cup/{id}")
    public ResponseEntity<Boolean> deleteCup(@PathVariable Long id) {
        try {
            boolean success = cupService.deleteCup(CupId.of(id));
            if (success) {
                return ResponseEntity.ok(Boolean.TRUE);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (DataIntegrityViolationException e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (IllegalArgumentException e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logError(e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
