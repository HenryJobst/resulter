package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.PersonKeyDto;
import de.jobst.resulter.adapter.driver.web.dto.SplitTimeAnalysisDto;
import de.jobst.resulter.application.SplitTimeAnalysisService;
import de.jobst.resulter.domain.ResultListId;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
public class SplitTimeAnalysisController {

    private final SplitTimeAnalysisService splitTimeAnalysisService;

    public SplitTimeAnalysisController(SplitTimeAnalysisService splitTimeAnalysisService) {
        this.splitTimeAnalysisService = splitTimeAnalysisService;
    }

    @GetMapping("/split_time_analysis/result_list/{id}/ranking")
    public ResponseEntity<List<SplitTimeAnalysisDto>> analyzeSplitTimesRanking(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") @Nullable Boolean mergeBidirectional,
            @RequestParam(required = false) @Nullable List<Long> filterPersonIds,
            @RequestParam(required = false, defaultValue = "false") @Nullable Boolean filterIntersection) {

        log.debug("Analyzing split times (ranking) for result list {} (merge: {}, person filters: {}, intersection: {})",
                id, mergeBidirectional, filterPersonIds, filterIntersection);

        List<SplitTimeAnalysisDto> analyses = splitTimeAnalysisService.analyzeSplitTimesRanking(
                        ResultListId.of(id),
                        Optional.ofNullable(mergeBidirectional).orElse(false),
                        Optional.ofNullable(filterPersonIds).orElse(List.of()),
                        Optional.ofNullable(filterIntersection).orElse(false)
                )
                .stream()
                .map(SplitTimeAnalysisDto::from)
                .toList();

        if (!analyses.isEmpty()) {
            long totalSegments = analyses.stream()
                    .mapToLong(a -> a.controlSegments().size())
                    .sum();
            long totalRunners = analyses.stream()
                    .flatMap(a -> a.controlSegments().stream())
                    .mapToLong(s -> s.runnerSplits().size())
                    .sum();
            log.info("Returning {} analysis/analyses with {} segments and {} total runner entries",
                    analyses.size(), totalSegments, totalRunners);
        }

        return ResponseEntity.ok(analyses);
    }

    @GetMapping("/split_time_analysis/result_list/{id}/persons")
    public ResponseEntity<List<PersonKeyDto>> getPersonsForResultList(@PathVariable Long id) {
        log.debug("Getting persons for result list {}", id);

        List<PersonKeyDto> persons = splitTimeAnalysisService.getPersonsForResultList(ResultListId.of(id))
                .stream()
                .map(PersonKeyDto::from)
                .toList();

        log.info("Returning {} persons for result list {}", persons.size(), id);

        return ResponseEntity.ok(persons);
    }
}
