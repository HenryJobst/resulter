package de.jobst.resulter.adapter.driver.web;

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
            @RequestParam(required = false) @Nullable List<String> filterNames) {

        log.debug("Analyzing split times (ranking) for result list {} (merge: {}, filters: {})",
                id, mergeBidirectional, filterNames);

        List<SplitTimeAnalysisDto> analyses = splitTimeAnalysisService.analyzeSplitTimesRanking(
                        ResultListId.of(id),
                        Optional.ofNullable(mergeBidirectional).orElse(false),
                        Optional.ofNullable(filterNames).orElse(List.of())
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

        return analyses.isEmpty()
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(analyses);
    }
}
