package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.application.port.ChampionshipFilterService;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.OrganisationId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class ChampionshipFilterController {

    private final ChampionshipFilterService championshipFilterService;

    public ChampionshipFilterController(ChampionshipFilterService championshipFilterService) {
        this.championshipFilterService = championshipFilterService;
    }

    /**
     * Marks all PersonRaceResults of non-eligible participants as NOT_COMPETING.
     * Eligibility: participant's club must be within the given base organisation's tree.
     */
    @PutMapping("/event/{id}/championship_cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> applyChampionshipCleanup(
            @PathVariable Long id,
            @RequestParam Long organisationId) {
        championshipFilterService.applyChampionshipCleanup(
                EventId.of(id), OrganisationId.of(organisationId));
        return ResponseEntity.noContent().build();
    }

    /**
     * Creates a Race-0 ResultList: eligible participants ranked first (state=OK),
     * non-eligible follow (state=NOT_COMPETING).
     */
    @PutMapping("/event/{id}/championship_ranking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addChampionshipRanking(
            @PathVariable Long id,
            @RequestParam Long organisationId) {
        championshipFilterService.addChampionshipRanking(
                EventId.of(id), OrganisationId.of(organisationId));
        return ResponseEntity.noContent().build();
    }
}
