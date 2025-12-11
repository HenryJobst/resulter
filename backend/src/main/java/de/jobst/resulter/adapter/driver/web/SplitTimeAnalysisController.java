package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.AnomalyAnalysisDto;
import de.jobst.resulter.adapter.driver.web.dto.MentalResilienceAnalysisDto;
import de.jobst.resulter.adapter.driver.web.dto.PersonKeyDto;
import de.jobst.resulter.adapter.driver.web.dto.SplitTimeAnalysisDto;
import de.jobst.resulter.application.port.AnomalyDetectionService;
import de.jobst.resulter.application.port.MentalResilienceService;
import de.jobst.resulter.application.port.SplitTimeRankingService;
import de.jobst.resulter.domain.ResultListId;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
public class SplitTimeAnalysisController {

    private final SplitTimeRankingService splitTimeRankingService;
    private final MentalResilienceService mentalResilienceService;
    private final AnomalyDetectionService anomalyDetectionService;

    public SplitTimeAnalysisController(
        SplitTimeRankingService splitTimeRankingService,
        MentalResilienceService mentalResilienceService, AnomalyDetectionService anomalyDetectionService) {
        this.splitTimeRankingService = splitTimeRankingService;
        this.mentalResilienceService = mentalResilienceService;
        this.anomalyDetectionService = anomalyDetectionService;
    }

    @GetMapping("/split_time_analysis/result_list/{id}/ranking")
    public ResponseEntity<List<SplitTimeAnalysisDto>> analyzeSplitTimesRanking(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") @Nullable Boolean mergeBidirectional,
            @RequestParam(required = false) @Nullable List<Long> filterPersonIds,
            @RequestParam(required = false, defaultValue = "false") @Nullable Boolean filterIntersection) {

        log.debug("Analyzing split times (ranking) for result list {} (merge: {}, person filters: {}, intersection: {})",
                id, mergeBidirectional, filterPersonIds, filterIntersection);

        List<SplitTimeAnalysisDto> analyses = splitTimeRankingService.analyzeSplitTimesRanking(
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

        List<PersonKeyDto> persons = splitTimeRankingService.getPersonsForResultList(ResultListId.of(id))
                .stream()
                .map(PersonKeyDto::from)
                .toList();

        log.info("Returning {} persons for result list {}", persons.size(), id);

        return ResponseEntity.ok(persons);
    }

    @GetMapping("/split_time_analysis/result_list/{id}/mental_resilience")
    public ResponseEntity<MentalResilienceAnalysisDto> analyzeMentalResilience(
            @PathVariable Long id,
            @RequestParam(required = false) @Nullable List<Long> filterPersonIds) {

        log.debug("Analyzing mental resilience for result list {} (person filters: {})",
                id, filterPersonIds);

        MentalResilienceAnalysisDto analysis = MentalResilienceAnalysisDto.from(
                mentalResilienceService.analyzeMentalResilience(
                        ResultListId.of(id),
                        Optional.ofNullable(filterPersonIds).orElse(List.of())
                )
        );

        if (analysis.statistics().totalMistakes() > 0) {
            log.info("Returning mental resilience analysis: {} runners, {} with mistakes, {} total mistakes, avg MRI: {}",
                    analysis.statistics().totalRunners(),
                    analysis.statistics().runnersWithMistakes(),
                    analysis.statistics().totalMistakes(),
                    analysis.statistics().averageMRI());
        } else {
            log.info("No mistakes detected in result list {}", id);
        }

        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/split_time_analysis/result_list/{id}/anomaly_detection")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnomalyAnalysisDto> anomalyDetection(
        @PathVariable Long id,
        @RequestParam(required = false) @Nullable List<Long> filterPersonIds) {

        log.debug("Anomaly detection for result list {} (person filters: {})",
            id, filterPersonIds);

        AnomalyAnalysisDto analysis = AnomalyAnalysisDto.from(
            anomalyDetectionService.analyzeAnomaly(
                ResultListId.of(id),
                Optional.ofNullable(filterPersonIds).orElse(List.of())));

        return ResponseEntity.ok(analysis);
    }
}
