package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.RaceDto;
import de.jobst.resulter.application.RaceService;
import de.jobst.resulter.domain.Race;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class RaceController {

    private final RaceService raceService;

    @Autowired
    public RaceController(RaceService raceService) {
        this.raceService = raceService;
    }

    @GetMapping("/race/all")
    public ResponseEntity<List<RaceDto>> getAllRaces() {
        List<Race> races = raceService.findAll();
        return ResponseEntity.ok(races.stream().map(RaceDto::from).toList());
    }

    @GetMapping("/race")
    public ResponseEntity<Page<RaceDto>> searchRaces(@RequestParam Optional<String> ignoredFilter, Pageable pageable) {
        // TODO: Implement filter and pageable
        List<Race> races = raceService.findAll();
        return ResponseEntity.ok(new PageImpl<>(
                races.stream().map(RaceDto::from).toList(),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()),
                races.size()));
    }
}
