package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.application.port.ChampionshipFilterService;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.OrganisationId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@Slf4j
public class ChampionshipFilterController {

    private final ChampionshipFilterService championshipFilterService;

    public ChampionshipFilterController(ChampionshipFilterService championshipFilterService) {
        this.championshipFilterService = championshipFilterService;
    }

    /**
     * Returns the distinct class result short names for all (non-race-0) result lists of the event.
     */
    @GetMapping("/event/{id}/championship_class_short_names")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Set<String>> getClassShortNames(@PathVariable Long id) {
        return ResponseEntity.ok(championshipFilterService.findClassShortNames(EventId.of(id)));
    }

    /**
     * Returns true if the event has more than one real race (i.e. race-0 is a true overall ranking).
     */
    @GetMapping("/event/{id}/championship_has_multiple_races")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> hasMultipleRaces(@PathVariable Long id) {
        return ResponseEntity.ok(championshipFilterService.hasMultipleRaces(EventId.of(id)));
    }

    /**
     * Marks non-eligible participants as NOT_COMPETING and reorders positions.
     *
     * @param excludeClassShortNames classes to skip; if omitted all classes are processed
     */
    @PutMapping("/event/{id}/championship_cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> applyChampionshipCleanup(
            @PathVariable Long id,
            @RequestParam Long organisationId,
            @RequestParam(required = false, defaultValue = "") List<String> excludeClassShortNames) {
        championshipFilterService.applyChampionshipCleanup(
                EventId.of(id), OrganisationId.of(organisationId), Set.copyOf(excludeClassShortNames));
        return ResponseEntity.noContent().build();
    }

    /**
     * Creates a Race-0 ResultList with championship ranking.
     *
     * @param excludeClassShortNames classes to skip; if omitted all classes are included
     */
    @PutMapping("/event/{id}/championship_ranking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addChampionshipRanking(
            @PathVariable Long id,
            @RequestParam Long organisationId,
            @RequestParam(required = false, defaultValue = "") List<String> excludeClassShortNames) {
        championshipFilterService.addChampionshipRanking(
                EventId.of(id), OrganisationId.of(organisationId), Set.copyOf(excludeClassShortNames));
        return ResponseEntity.noContent().build();
    }
}
