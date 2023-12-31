package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.CupDto;
import de.jobst.resulter.application.CupService;
import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.CupConfig;
import de.jobst.resulter.domain.EventConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class CupController {

    private final CupService cupService;

    @Autowired
    public CupController(CupService cupService) {
        this.cupService = cupService;
    }

    @GetMapping("/cup")
    public ResponseEntity<List<CupDto>> handleCups(
            @RequestParam(name = "shallowEvents", required = false, defaultValue = "true")
            Boolean shallowEvents
    ) {
        try {
            CupConfig cupConfig = CupService.getCupConfig(shallowEvents, EventConfig.empty());
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
}
