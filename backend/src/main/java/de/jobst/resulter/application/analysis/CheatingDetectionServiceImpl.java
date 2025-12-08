package de.jobst.resulter.application.analysis;


import de.jobst.resulter.application.port.CheatingDetectionService;
import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.application.port.SegmentPI;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.analysis.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.jobst.resulter.application.analysis.SplitTimeAnalysisServiceImpl.*;

@Service
@Slf4j
public class CheatingDetectionServiceImpl implements CheatingDetectionService {

    // Must match the constant used in MentalResilienceServiceImpl for consistency
    private static final int TOP_RUNNERS_FOR_REFERENCE = 3;

    private final SplitTimeListRepository splitTimeListRepository;
    private final ResultListRepository resultListRepository;
    private final SplitTimeAnalysisServiceImpl splitTimeAnalysisService;

    public CheatingDetectionServiceImpl(SplitTimeListRepository splitTimeListRepository,
                                        ResultListRepository resultListRepository,
                                        SplitTimeAnalysisServiceImpl splitTimeAnalysisService) {
        this.splitTimeListRepository = splitTimeListRepository;
        this.resultListRepository = resultListRepository;
        this.splitTimeAnalysisService = splitTimeAnalysisService;
    }

    private List<AnomaliesIndex> analyzeRunnerForCheating(
        PersonId runnerId,
        String className,
        List<SegmentPI> segmentPIs,
        PerformanceIndex normalPI,
        Map<SegmentKey, List<Double>> allSegmentTimesBySegmentKey) {

        List<AnomaliesIndex> anomalies = new ArrayList<>();

        for (SegmentPI segmentPI : segmentPIs) {
            AnomaliesIndex ai = analyzeSegment(className, segmentPI, normalPI, allSegmentTimesBySegmentKey);

            // Collect only suspicious segments
            if (ai.classification() != CheatingClassification.NO_SUSPICION) {
                anomalies.add(ai);
            }
        }

        return anomalies;
    }

    /**
     * Analyzes a single segment for cheating based on the Anomalie Index (AI).
     */
    private AnomaliesIndex analyzeSegment(
        String className,
        SegmentPI segmentPI,
        PerformanceIndex normalPI,
        Map<SegmentKey, List<Double>> allSegmentTimesBySegmentKey) {

        // 1. Calculate the Cleaned Reference Time
        SegmentKey segmentKey = new SegmentKey(className,segmentPI.fromControl(), segmentPI.toControl());
        double cleanedReferenceTime = calculateCleanedReferenceTime(
            segmentKey,
            segmentPI.runnerTime(), // Pass the runner's time directly for targeted exclusion
            allSegmentTimesBySegmentKey);

        if (cleanedReferenceTime <= 0) {
            return AnomaliesIndex.of(segmentPI.legNumber(), segmentKey, segmentPI.pi(), normalPI,
                CheatingClassification.NO_DATA);
        }

        // 2. Calculate PI based on the Cleaned Reference (PI_Real)
        PerformanceIndex piReal = PerformanceIndex.of(segmentPI.runnerTime(), cleanedReferenceTime);

        // 3. Define the Expected PI (PI_Expected)
        @SuppressWarnings("UnnecessaryLocalVariable")
        PerformanceIndex piExpected = normalPI;

        // 4. Calculate the Anomalie Index (AI)
        double aiValue = piReal.value() / piExpected.value();

        CheatingClassification classification = classifyAnomalies(piReal.value(), aiValue);

        return AnomaliesIndex.of(segmentPI.legNumber(),segmentKey, piReal, piExpected, classification);
    }

    /**
     * Calculates the reference time excluding the specific time of the runner being analyzed.
     * This prevents a cheating time from contaminating the benchmark.
     */
    private double calculateCleanedReferenceTime(
        SegmentKey segmentKey,
        double runnerTime,
        Map<SegmentKey, List<Double>> allSegmentTimesBySegmentKey) {

        List<Double> allTimes = allSegmentTimesBySegmentKey.getOrDefault(segmentKey, Collections.emptyList());

        if (allTimes.isEmpty()) {
            return 0.0;
        }

        // Create a mutable copy of times and remove the exact time of the runner.
        // NOTE: This assumes unique times or that the runner's time only appears once.
        List<Double> filteredTimes = allTimes.stream()
            .filter(time -> time != runnerTime)
            .collect(Collectors.toList());

        if (filteredTimes.isEmpty()) {
            return 0.0;
        }

        Collections.sort(filteredTimes);

        // Take average of the top N times (or all if fewer remaining runners)
        int topN = Math.min(TOP_RUNNERS_FOR_REFERENCE, filteredTimes.size());

        double cleanedReferenceTime = filteredTimes.stream()
            .limit(topN)
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);

        return cleanedReferenceTime > 0 ? cleanedReferenceTime : 0.0;
    }

    /**
     * Classifies the anomaly based on absolute and individual thresholds.
     */
    private CheatingClassification classifyAnomalies(double piReal, double aiValue) {
        // Criteria A: Absolute Anomalies (much faster than top 3 average)
        // Criteria B: Individual Anomalies (much faster than own baseline)

        final double ABSOLUTE_ANOMALY_THRESHOLD = 0.80; // E.g., 20% faster than the next top 3 average
        final double INDIVIDUAL_ANOMALY_THRESHOLD = 0.75; // E.g., 25% faster than runner's Normal PI

        final double MODERATE_ABSOLUTE_THRESHOLD = 0.90;
        final double MODERATE_INDIVIDUAL_THRESHOLD = 0.85;

        if (piReal < ABSOLUTE_ANOMALY_THRESHOLD && aiValue < INDIVIDUAL_ANOMALY_THRESHOLD) {
            return CheatingClassification.HIGH_SUSPICION;
        }

        if (piReal < MODERATE_ABSOLUTE_THRESHOLD && aiValue < MODERATE_INDIVIDUAL_THRESHOLD) {
            return CheatingClassification.MODERATE_SUSPICION;
        }

        return CheatingClassification.NO_SUSPICION;
    }

    @Override
    public CheatingAnalysis analyzeCheating(ResultListId resultListId, List<Long> filterPersonIds) {
        long startTime = System.currentTimeMillis();
        log.debug("Starting mental resilience analysis for result list {} with person filter: {}",
            resultListId, filterPersonIds);

        // Step 1: Fetch split time data
        List<SplitTimeList> splitTimeLists = splitTimeListRepository.findByResultListId(resultListId);
        log.debug("Fetched {} split time lists", splitTimeLists.size());

        if (splitTimeLists.isEmpty()) {
            log.info("No split time data found for result list {}", resultListId);
            return createEmptyAnalysis(resultListId);
        }

        // Step 2: Get event ID from first split time list
        EventId eventId = splitTimeLists.getFirst().getEventId();

        // Step 3: Fetch result list for runtime data
        ResultList resultList = resultListRepository.findById(resultListId)
            .orElseThrow(() -> new IllegalArgumentException("Result list not found: " + resultListId));

        // Step 4: Build runtime map
        Map<RuntimeKey, Double> runtimeMap = splitTimeAnalysisService.buildRuntimeMap(resultList);
        log.debug("Built runtime map with {} entries", runtimeMap.size());

        // Step 5: Count runners per class
        Map<String, Integer> runnersPerClass = splitTimeAnalysisService.countRunnersPerClass(splitTimeLists);
        log.debug("Runner count per class: {}", runnersPerClass);

        // Step 6: Calculate reference times per segment
        Map<SegmentKey, Double> referenceTimesPerSegment =
            splitTimeAnalysisService.calculateReferenceTimesPerSegment(splitTimeLists, runtimeMap);
        log.debug("Calculated reference times for {} segments", referenceTimesPerSegment.size());

        // Step 7: Analyze each or only filtered runners
        Set<Long> filterPersonIdSet = new HashSet<>(filterPersonIds);

        // define the predicate (filter condition) separately
        Predicate<SplitTimeList> personIdFilter = stl ->
            filterPersonIdSet.isEmpty() || filterPersonIdSet.contains(stl.getPersonId().value());

        // define the analysis function
        Map<SegmentKey, List<Double>> allSegmentTimesBySegmentKey =
            splitTimeAnalysisService.calculateAllTimesPerSegment(splitTimeLists, runtimeMap);
        Function<SplitTimeList, Optional<RunnerCheatingProfile>> runnerAnalyzer = stl ->
            analyzeRunner(stl, referenceTimesPerSegment, runtimeMap, runnersPerClass, allSegmentTimesBySegmentKey);

        List<RunnerCheatingProfile> runnerProfiles = splitTimeLists.stream()
            .filter(personIdFilter)
            .map(runnerAnalyzer)
            .flatMap(Optional::stream)
            .toList();

        log.debug("Analyzed {} runners", runnerProfiles.size());

        return new CheatingAnalysis(resultListId, eventId, runnerProfiles);
    }

    private CheatingAnalysis createEmptyAnalysis(ResultListId resultListId) {
        EventId eventId = new EventId(0L); // Placeholder, will be overridden if data exists
        return new CheatingAnalysis(resultListId, eventId, List.of());
    }

    /**
     * Analyzes a single runner and creates their cheating profile.
     * Returns Optional.empty() if the class has too few runners for reliable analysis.
     */
    private Optional<RunnerCheatingProfile> analyzeRunner(
        SplitTimeList splitTimeList,
        Map<SegmentKey, Double> referenceTimesPerSegment,
        Map<RuntimeKey, Double> runtimeMap,
        Map<String, Integer> runnersPerClass,
        Map<SegmentKey, List<Double>> allSegmentTimesBySegmentKey
        ) {

        PersonId personId = splitTimeList.getPersonId();
        String className = splitTimeList.getClassResultShortName().value();
        RaceNumber raceNumber = splitTimeList.getRaceNumber();

        // Check minimum runner threshold
        int classRunnerCount = runnersPerClass.getOrDefault(className, 0);
        if (classRunnerCount < MIN_RUNNERS_PER_CLASS_FOR_ANALYSIS) {
            log.info("Skipping calculation for runner {} in class {} - only {} runners (min: {})",
                personId, className, classRunnerCount, MIN_RUNNERS_PER_CLASS_FOR_ANALYSIS);
            return Optional.empty();
        }

        // Calculate segment times
        List<SegmentTime> segmentTimes = splitTimeAnalysisService.calculateSegmentTimes(splitTimeList, runtimeMap);

        if (segmentTimes.isEmpty()) {
            log.debug("No valid segments for runner {}", personId);
            return Optional.empty();
        }

        // Calculate Performance Index for each segment
        List<SegmentPI> segmentPIs =
            splitTimeAnalysisService.calculateSegmentPIs(segmentTimes, referenceTimesPerSegment, className);

        // Calculate Normal PI (average excluding mistakes)
        PerformanceIndex normalPI = splitTimeAnalysisService.calculateNormalPI(segmentPIs);

        // Check if we have enough non-mistake segments to establish a baseline
        if (normalPI == null) {
            log.info("Skipping calculation for runner {} in class {} - too many mistakes (< {} non-mistake segments)",
                personId, className, MIN_NON_MISTAKE_SEGMENTS);
            return Optional.empty();
        }

        List<AnomaliesIndex> anomaliesIndexList =
            analyzeRunnerForCheating(personId, className, segmentPIs, normalPI, allSegmentTimesBySegmentKey);

        if (anomaliesIndexList.isEmpty()) {
            log.debug("No anomalies detected for runner {} in class {}", personId, className);
            return Optional.empty();
        }

        boolean reliableData = classRunnerCount >= RELIABLE_RUNNERS_THRESHOLD;

        Optional<AnomaliesIndex> minAnomaliesIndex =
            anomaliesIndexList.stream().min((x, y) -> Objects.compare(x.aiValue(), y.aiValue(), Double::compare));

        return Optional.of(new RunnerCheatingProfile(
            personId,
            className,
            raceNumber,
            classRunnerCount,
            reliableData,
            normalPI,
            minAnomaliesIndex.get().aiValue(),
            minAnomaliesIndex.get().legNumber(),
            anomaliesIndexList.stream().map(x -> new AnomaliesIndexInformation(x.legNumber(),
                ControlCode.of(x.segmentKey().fromControl()), ControlCode.of(x.segmentKey().toControl()), x.piReal(), x,
                x.classification())).toList(),
            minAnomaliesIndex.get().classification()
        ));
    }
}
