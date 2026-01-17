package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.adapter.driver.web.dto.AnomalyAnalysisDto;
import de.jobst.resulter.adapter.driver.web.dto.ClassGroupOptionDto;
import de.jobst.resulter.adapter.driver.web.dto.CourseGroupOptionDto;
import de.jobst.resulter.adapter.driver.web.dto.HangingAnalysisDto;
import de.jobst.resulter.adapter.driver.web.dto.MentalResilienceAnalysisDto;
import de.jobst.resulter.adapter.driver.web.dto.PersonKeyDto;
import de.jobst.resulter.adapter.driver.web.dto.SplitTimeAnalysisDto;
import de.jobst.resulter.adapter.driver.web.dto.SplitTimeTableDto;
import de.jobst.resulter.adapter.driver.web.dto.SplitTimeTableOptionsDto;
import de.jobst.resulter.adapter.driver.web.mapper.PersonKeyMapper;
import de.jobst.resulter.application.port.AnomalyDetectionService;
import de.jobst.resulter.application.port.HangingDetectionService;
import de.jobst.resulter.application.port.MentalResilienceService;
import de.jobst.resulter.application.port.SplitTimeRankingService;
import de.jobst.resulter.application.port.SplitTimeTableService;
import de.jobst.resulter.domain.ResultListId;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class SplitTimeAnalysisController {

    private final SplitTimeRankingService splitTimeRankingService;
    private final MentalResilienceService mentalResilienceService;
    private final AnomalyDetectionService anomalyDetectionService;
    private final HangingDetectionService hangingDetectionService;
    private final SplitTimeTableService splitTimeTableService;

    public SplitTimeAnalysisController(
            SplitTimeRankingService splitTimeRankingService,
            MentalResilienceService mentalResilienceService,
            AnomalyDetectionService anomalyDetectionService,
            HangingDetectionService hangingDetectionService,
            SplitTimeTableService splitTimeTableService) {
        this.splitTimeRankingService = splitTimeRankingService;
        this.mentalResilienceService = mentalResilienceService;
        this.anomalyDetectionService = anomalyDetectionService;
        this.hangingDetectionService = hangingDetectionService;
        this.splitTimeTableService = splitTimeTableService;
    }

    @GetMapping("/split_time_analysis/result_list/{id}/ranking")
    public ResponseEntity<List<SplitTimeAnalysisDto>> analyzeSplitTimesRanking(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") @Nullable Boolean mergeBidirectional,
            @RequestParam(required = false) @Nullable List<Long> filterPersonIds,
            @RequestParam(required = false, defaultValue = "false") @Nullable Boolean filterIntersection) {

        log.debug(
                "Analyzing split times (ranking) for result list {} (merge: {}, person filters: {}, intersection: {})",
                id,
                mergeBidirectional,
                filterPersonIds,
                filterIntersection);

        List<SplitTimeAnalysisDto> analyses = splitTimeRankingService
                .analyzeSplitTimesRanking(
                        ResultListId.of(id),
                        Optional.ofNullable(mergeBidirectional).orElse(false),
                        Optional.ofNullable(filterPersonIds).orElse(List.of()),
                        Optional.ofNullable(filterIntersection).orElse(false))
                .stream()
                .map(SplitTimeAnalysisDto::from)
                .toList();

        if (!analyses.isEmpty()) {
            long totalSegments =
                    analyses.stream().mapToLong(a -> a.controlSegments().size()).sum();
            long totalRunners = analyses.stream()
                    .flatMap(a -> a.controlSegments().stream())
                    .mapToLong(s -> s.runnerSplits().size())
                    .sum();
            log.info(
                    "Returning {} analysis/analyses with {} segments and {} total runner entries",
                    analyses.size(),
                    totalSegments,
                    totalRunners);
        }

        return ResponseEntity.ok(analyses);
    }

    @GetMapping("/split_time_analysis/result_list/{id}/persons")
    public ResponseEntity<List<PersonKeyDto>> getPersonsForResultList(@PathVariable Long id) {
        log.debug("Getting persons for result list {}", id);

        List<PersonKeyDto> persons = splitTimeRankingService.getPersonsForResultList(ResultListId.of(id)).stream()
                .map(PersonKeyMapper::toDto)
                .toList();

        log.info("Returning {} persons for result list {}", persons.size(), id);

        return ResponseEntity.ok(persons);
    }

    @GetMapping("/split_time_analysis/result_list/{id}/mental_resilience")
    public ResponseEntity<MentalResilienceAnalysisDto> analyzeMentalResilience(
            @PathVariable Long id, @RequestParam(required = false) @Nullable List<Long> filterPersonIds) {

        log.debug("Analyzing mental resilience for result list {} (person filters: {})", id, filterPersonIds);

        MentalResilienceAnalysisDto analysis =
                MentalResilienceAnalysisDto.from(mentalResilienceService.analyzeMentalResilience(
                        ResultListId.of(id),
                        Optional.ofNullable(filterPersonIds).orElse(List.of())));

        if (analysis.statistics().totalMistakes() > 0) {
            log.info(
                    "Returning mental resilience analysis: {} runners, {} with mistakes, {} total mistakes, avg MRI: {}",
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
            @PathVariable Long id, @RequestParam(required = false) @Nullable List<Long> filterPersonIds) {

        log.debug("Anomaly detection for result list {} (person filters: {})", id, filterPersonIds);

        AnomalyAnalysisDto analysis = AnomalyAnalysisDto.from(anomalyDetectionService.analyzeAnomaly(
                ResultListId.of(id), Optional.ofNullable(filterPersonIds).orElse(List.of())));

        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/split_time_analysis/result_list/{id}/hanging_detection")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HangingAnalysisDto> hangingDetection(
            @PathVariable Long id, @RequestParam(required = false) @Nullable List<Long> filterPersonIds) {

        log.debug("Hanging detection for result list {} (person filters: {})", id, filterPersonIds);

        HangingAnalysisDto analysis = HangingAnalysisDto.from(hangingDetectionService.analyzeHanging(
                ResultListId.of(id), Optional.ofNullable(filterPersonIds).orElse(List.of())));

        if (analysis.statistics().totalHangingSegments() > 0) {
            log.info(
                    "Hanging analysis: {} runners, {} with hanging, {} segments, avg HI: {}",
                    analysis.statistics().totalRunners(),
                    analysis.statistics().runnersWithHanging(),
                    analysis.statistics().totalHangingSegments(),
                    analysis.statistics().averageHangingIndex());
        } else {
            log.info("No hanging behavior detected in result list {}", id);
        }

        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/split_time_analysis/result_list/{id}/split_table")
    public ResponseEntity<SplitTimeTableDto> getSplitTimeTable(
            @PathVariable Long id, @RequestParam String groupBy, @RequestParam String groupId) {

        log.debug("Generating split-time table for result list {} (groupBy: {}, groupId: {})", id, groupBy, groupId);

        SplitTimeTableDto table;
        if ("class".equalsIgnoreCase(groupBy)) {
            table = SplitTimeTableDto.from(splitTimeTableService.generateByClass(ResultListId.of(id), groupId));
        } else if ("course".equalsIgnoreCase(groupBy)) {
            table = SplitTimeTableDto.from(
                    splitTimeTableService.generateByCourse(ResultListId.of(id), Long.parseLong(groupId)));
        } else {
            throw new IllegalArgumentException(
                    "Invalid groupBy parameter: " + groupBy + ". Must be 'class' or 'course'");
        }

        log.info(
                "Returning split-time table: {} runners, {} controls, {} complete splits",
                table.metadata().totalRunners(),
                table.metadata().totalControls(),
                table.metadata().runnersWithCompleteSplits());

        return ResponseEntity.ok(table);
    }

    @GetMapping("/split_time_analysis/result_list/{id}/split_table/options")
    public ResponseEntity<SplitTimeTableOptionsDto> getSplitTableOptions(@PathVariable Long id) {
        log.debug("Getting split-time table options for result list {}", id);

        List<ClassGroupOptionDto> classes = splitTimeTableService.getAvailableClasses(ResultListId.of(id)).stream()
                .map(ClassGroupOptionDto::from)
                .toList();

        List<CourseGroupOptionDto> courses = splitTimeTableService.getAvailableCourses(ResultListId.of(id)).stream()
                .map(CourseGroupOptionDto::from)
                .toList();

        SplitTimeTableOptionsDto options = new SplitTimeTableOptionsDto(classes, courses);

        log.info("Returning {} classes and {} courses", classes.size(), courses.size());

        return ResponseEntity.ok(options);
    }
}
