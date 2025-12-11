package de.jobst.resulter.application.analysis;


import de.jobst.resulter.application.port.AnomalyDetectionService;
import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.application.port.SegmentPI;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.analysis.*;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.jobst.resulter.application.analysis.SplitTimeAnalysisServiceImpl.*;

@Service
@Slf4j
public class AnomalyDetectionServiceImpl implements AnomalyDetectionService {

    // Must match the constant used in MentalResilienceServiceImpl for consistency
    private static final int TOP_RUNNERS_FOR_REFERENCE = 3;

    // Minimum runners in class before falling back to cross-class comparison
    private static final int MIN_RUNNERS_FOR_CLASS_SPECIFIC = 5;

    // Epsilon for floating-point comparison (0.001 seconds = 1 millisecond tolerance)
    private static final double EPSILON = 0.001;

    private final @Nullable SplitTimeListRepository splitTimeListRepository;
    private final @Nullable ResultListRepository resultListRepository;
    private final @Nullable SplitTimeAnalysisServiceImpl splitTimeAnalysisService;

    public AnomalyDetectionServiceImpl(@Nullable SplitTimeListRepository splitTimeListRepository,
                                        @Nullable ResultListRepository resultListRepository,
                                        @Nullable SplitTimeAnalysisServiceImpl splitTimeAnalysisService) {
        this.splitTimeListRepository = splitTimeListRepository;
        this.resultListRepository = resultListRepository;
        this.splitTimeAnalysisService = splitTimeAnalysisService;
    }

    private List<AnomaliesIndex> analyzeRunnerForAnomaly(
        String className,
        List<SegmentPI> segmentPIs,
        PerformanceIndex normalPI,
        Map<SegmentKey, List<Double>> allSegmentTimesBySegmentKey,
        Map<SegmentKey, List<Double>> allSegmentTimesCrossClass) {

        List<AnomaliesIndex> anomalies = new ArrayList<>();

        for (SegmentPI segmentPI : segmentPIs) {
            // Skip first segment - high variance especially for inexperienced runners
            if (segmentPI.fromControl().equals(START_CODE)) {
                log.trace("Skipping first segment for anomaly detection");
                continue;
            }

            // Skip final segment - it's excluded from normalPI calculation and is often much faster
            if (segmentPI.toControl().equals(FINAL_CODE)) {
                log.trace("Skipping final segment for anomaly detection (leg {})", segmentPI.legNumber());
                continue;
            }

            AnomaliesIndex ai = analyzeSegment(
                className,
                segmentPI,
                normalPI,
                allSegmentTimesBySegmentKey,
                allSegmentTimesCrossClass
            );

            // Collect only suspicious segments (exclude NO_SUSPICION and NO_DATA)
            if (ai.classification() != AnomalyClassification.NO_SUSPICION
                    && ai.classification() != AnomalyClassification.NO_DATA) {
                anomalies.add(ai);
            }
        }

        return anomalies;
    }

    /**
     * Analyzes a single segment for anomaly based on the Anomalie Index (AI).
     * Falls back to cross-class comparison if the runner's class has too few participants.
     */
    private AnomaliesIndex analyzeSegment(
        String className,
        SegmentPI segmentPI,
        PerformanceIndex normalPI,
        Map<SegmentKey, List<Double>> allSegmentTimesBySegmentKey,
        Map<SegmentKey, List<Double>> allSegmentTimesCrossClass) {

        // 1. Calculate the Cleaned Reference Time with cross-class fallback
        SegmentKey classSpecificKey = new SegmentKey(className, segmentPI.fromControl(), segmentPI.toControl());
        SegmentKey crossClassKey = new SegmentKey(null, segmentPI.fromControl(), segmentPI.toControl());

        double cleanedReferenceTime = calculateCleanedReferenceTimeWithFallback(
            classSpecificKey,
            crossClassKey,
            segmentPI.runnerTime(),
            allSegmentTimesBySegmentKey,
            allSegmentTimesCrossClass
        );

        if (cleanedReferenceTime <= 0) {
            return AnomaliesIndex.of(segmentPI.legNumber(), classSpecificKey, segmentPI.pi(), normalPI,
                AnomalyClassification.NO_DATA, 0.0);
        }

        // 2. Calculate PI based on the Cleaned Reference (PI_Real)
        PerformanceIndex piReal = PerformanceIndex.of(segmentPI.runnerTime(), cleanedReferenceTime);

        // 3. Define the Expected PI (PI_Expected)
        @SuppressWarnings("UnnecessaryLocalVariable")
        PerformanceIndex piExpected = normalPI;

        // 4. Calculate the Anomalie Index (AI)
        double aiValue = piReal.value() / piExpected.value();

        // 5. Calculate absolute time difference (how many seconds faster)
        double timeDifference = cleanedReferenceTime - segmentPI.runnerTime();

        AnomalyClassification classification = classifyAnomalies(
            piReal.value(),
            aiValue,
            timeDifference,
            cleanedReferenceTime
        );

        return AnomaliesIndex.of(segmentPI.legNumber(), classSpecificKey, piReal, piExpected, classification, cleanedReferenceTime);
    }

    /**
     * Calculates reference time with fallback to cross-class times for small classes.
     * Tries class-specific times first, falls back to all classes if too few runners.
     */
    private double calculateCleanedReferenceTimeWithFallback(
            SegmentKey classSpecificKey,
            SegmentKey crossClassKey,
            double runnerTime,
            Map<SegmentKey, List<Double>> classSpecificTimes,
            Map<SegmentKey, List<Double>> crossClassTimes) {

        // Try class-specific times first
        List<Double> times = classSpecificTimes.getOrDefault(classSpecificKey, Collections.emptyList());

        // If too few runners in class, fall back to cross-class times
        if (times.size() < MIN_RUNNERS_FOR_CLASS_SPECIFIC) {
            log.debug("Class {} has only {} runners on segment {} → {}, falling back to cross-class times",
                    classSpecificKey.className(),
                    times.size(),
                    classSpecificKey.fromControl(),
                    classSpecificKey.toControl());
            times = crossClassTimes.getOrDefault(crossClassKey, Collections.emptyList());
        }

        return calculateCleanedReferenceTime(runnerTime, times);
    }

    /**
     * Calculates the reference time excluding the specific time of the runner being analyzed.
     * Uses MEDIAN of top performers to be robust against multiple runners using shortcuts.
     * <p>
     * IMPORTANT: If multiple runners take a shortcut, the TOP-3 average would be contaminated.
     * The median is more robust against outlier groups (e.g., 3 runners taking a shortcut).
     */
    private double calculateCleanedReferenceTime(double runnerTime, List<Double> allTimes) {
        if (allTimes.isEmpty()) {
            return 0.0;
        }

        // Create a mutable copy of times and remove the exact time of the runner.
        // Use epsilon-based comparison for floating-point values
        List<Double> filteredTimes = allTimes.stream()
                .filter(time -> Math.abs(time - runnerTime) > EPSILON)
                .collect(Collectors.toList());

        if (filteredTimes.isEmpty()) {
            return 0.0;
        }

        Collections.sort(filteredTimes);

        // Check for possible group shortcut: If top times cluster together and are much faster
        // Pass runnerTime to allow position-aware analysis
        double cleanedReferenceTime = calculateRobustReferenceTime(filteredTimes, runnerTime);

        return cleanedReferenceTime > 0 ? cleanedReferenceTime : 0.0;
    }

    /**
     * Calculates a robust reference time that's resistant to:
     * 1. Groups of runners using shortcuts (top times clustering)
     * 2. Majority of runners making mistakes (high variance in slower times)
     * <p>
     * Strategy:
     * - Detect if fastest time is significantly faster than others (possible mistake by majority)
     * - Detect if top times cluster together but are separated from rest (possible group shortcut)
     * - Use appropriate reference based on detection results
     * <p>
     * IMPORTANT: sortedTimes has already been filtered to exclude runnerTime.
     * We need to account for position shifts if runnerTime was among the top runners.
     *
     * @param sortedTimes List of times (already sorted, excluding runnerTime)
     * @param runnerTime The time of the runner being analyzed (already removed from sortedTimes)
     */
    private double calculateRobustReferenceTime(List<Double> sortedTimes, double runnerTime) {
        if (sortedTimes.isEmpty()) {
            return 0.0;
        }

        // For very small datasets, use the fastest time to avoid false positives
        if (sortedTimes.size() <= 2) {
            return sortedTimes.getFirst();
        }

        // Determine where runnerTime would have been positioned in the original sorted list
        // This tells us if we need to adjust indices for cluster analysis
        int runnerPositionInOriginal = 0;
        for (Double time : sortedTimes) {
            if (runnerTime < time) {
                break;
            }
            runnerPositionInOriginal++;
        }

        // Take top 3 runners (or all if fewer) for analysis
        List<Double> topTimes = sortedTimes.subList(0, Math.min(TOP_RUNNERS_FOR_REFERENCE, sortedTimes.size()));

        // CASE 1: Detect if MAJORITY made a mistake (reverse outlier detection)
        // If the fastest time is much faster than median of all times, and the slower times
        // have high variance, this suggests the majority made a mistake, not that the fastest cheated
        if (sortedTimes.size() >= 5) {
            double fastest = sortedTimes.get(0);
            double second = sortedTimes.get(1);
            double median = sortedTimes.get(sortedTimes.size() / 2);

            // Check if fastest is MUCH faster than median (> 50% faster)
            // AND second-fastest is also much faster (both are fast)
            // This suggests the slower times might be mistakes
            double fastestVsMedianGap = (median - fastest) / fastest;
            double secondVsMedianGap = (median - second) / fastest;

            if (fastestVsMedianGap > 0.50 && secondVsMedianGap > 0.40) {
                // Check variance in slower times (positions 3-end)
                if (sortedTimes.size() >= 6) {
                    List<Double> slowerTimes = sortedTimes.subList(2, sortedTimes.size());
                    double avgSlower = slowerTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                    double variance = slowerTimes.stream()
                        .mapToDouble(t -> Math.pow(t - avgSlower, 2))
                        .average()
                        .orElse(0);
                    double stdDev = Math.sqrt(variance);
                    double coefficientOfVariation = avgSlower > 0 ? (stdDev / avgSlower) : 0;

                    // If slower times have high variance (CV > 0.15), likely mistakes
                    if (coefficientOfVariation > 0.15) {
                        log.debug("Majority mistake detected (fastest vs median: {}, CV: {}), using top-2 average",
                            String.format("%.1f%%", fastestVsMedianGap * 100),
                            String.format("%.2f", coefficientOfVariation));

                        // Use average of top 2 times as reference (they ran correctly)
                        return (fastest + second) / 2.0;
                    }
                }
            }
        }

        // CASE 2: Detect suspicious clustering in top times (group shortcut)
        // If the gap between fastest and 4th-fastest is small, but gap to 5th+ is large,
        // this suggests a group might be using a shortcut
        //
        // IMPORTANT: Adjust indices if runnerTime was among the top 4 in the original list
        // If runnerTime was position 0-3, then what we see as position 4+ in sortedTimes
        // was actually position 5+ in the original list

        // Adjust indices based on where the runner's time was in the original list
        // If runner was in top 4 (positions 0-3), we need to shift our analysis window
        int indexAdjustment = (runnerPositionInOriginal <= 3) ? 1 : 0;

        // We need at least 8 times AFTER adjustment to perform this analysis
        // (positions 0-7 after adjustment)
        if (sortedTimes.size() >= (8 + indexAdjustment)) {
                int fastestIdx = 0;
                int fourthIdx = 3 + indexAdjustment;
                int sixthIdx = 5 + indexAdjustment;
                int seventhIdx = 7 + indexAdjustment;  // Position 7 = Index 7 (8th element)

                double fastest = sortedTimes.get(fastestIdx);
                double fourth = sortedTimes.get(fourthIdx);
                double sixth = sortedTimes.get(sixthIdx);
                double seventh = sortedTimes.get(seventhIdx);

                // Check if top 3-4 times cluster together (< 10% spread)
                // but are separated from the rest (> 25% gap to position 7)
                double topClusterSpread = (fourth - fastest) / fastest;
                double gapToRest = (seventh - fourth) / fourth;

                // IMPORTANT: Check if position 7 is itself an outlier (e.g., runner made a big mistake)
                // If the gap between position 6 and 7 is very large (> 40%), then position 7 is likely
                // an outlier, NOT evidence that top 4 runners used a shortcut
                double gapSixToSeven = (seventh - sixth) / sixth;

                // Only trigger cluster detection if:
                // 1. Top times cluster together (< 10% spread)
                // 2. Gap to rest is significant (> 25%)
                // 3. Position 8 is NOT an outlier (gap 7→8 is reasonable, < 40%)
                if (topClusterSpread < 0.10 && gapToRest > 0.25 && gapSixToSeven < 0.40) {
                    // Suspicious cluster detected - possible group shortcut
                    // Use positions 5-8 (indices 4-7) instead of top 4 times (with adjustment)
                    log.debug("Suspicious time cluster detected (top spread: {}, gap: {}, gap7-8: {}, indexAdjustment: {}), using positions 5-8 for reference",
                            String.format("%.1f%%", topClusterSpread * 100),
                            String.format("%.1f%%", gapToRest * 100),
                            String.format("%.1f%%", gapSixToSeven * 100),
                            indexAdjustment);

                    int saferStartIdx = 4 + indexAdjustment;  // Position 5 (index 4)
                    int saferEndIdx = Math.min(8 + indexAdjustment, sortedTimes.size());  // Up to position 8 (index 7)

                    List<Double> saferTimes = sortedTimes.subList(saferStartIdx, saferEndIdx);
                    return saferTimes.stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);
                } else if (topClusterSpread < 0.10 && gapToRest > 0.25 && gapSixToSeven >= 0.40) {
                    // Position 8 is an outlier - use positions 5-7 (indices 4-6) instead (excluding the outlier)
                    log.debug("Suspicious cluster detected BUT position 8 is an outlier (gap7-8: {}), using positions 5-7 for reference",
                            String.format("%.1f%%", gapSixToSeven * 100));

                    int saferStartIdx = 4 + indexAdjustment;  // Position 5 (index 4)
                    int saferEndIdx = Math.min(7 + indexAdjustment, sortedTimes.size());  // Up to position 7 (index 6)

                    List<Double> saferTimes = sortedTimes.subList(saferStartIdx, saferEndIdx);
                    return saferTimes.stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);
                }
        }

        // CASE 3: No special condition detected - use median of top performers
        // Median is more robust than average against individual outliers
        int medianIndex = topTimes.size() / 2;
        if (topTimes.size() % 2 == 0) {
            // Even number: average of two middle values
            return (topTimes.get(medianIndex - 1) + topTimes.get(medianIndex)) / 2.0;
        } else {
            // Odd number: middle value
            return topTimes.get(medianIndex);
        }
    }

    /**
     * Classifies anomalies using THREE criteria with dynamic thresholds:
     * 1. Relative to top-3 runners (piReal)
     * 2. Relative to own baseline (aiValue)
     * 3. Absolute time difference (scaled by segment length)
     * <p>
     * IMPORTANT: Uses segment-length-aware thresholds to account for:
     * - Fast segments (roads/paths): Elite runners are proportionally faster
     * - Long segments: Require proportionally larger time savings
     * - Short segments: Fixed minimum to avoid noise
     */
    private AnomalyClassification classifyAnomalies(
            double piReal,
            double aiValue,
            double timeDifference,
            double referenceTime) {

        // Dynamic time thresholds based on segment length
        // Base threshold: 20% of reference time (increased from 15% to be more conservative)
        final double TIME_THRESHOLD_PERCENTAGE = 0.20; // 20% of segment time
        final double MIN_TIME_THRESHOLD = 40.0;        // Minimum 40s (increased from 25s)
        final double MAX_TIME_THRESHOLD = 150.0;       // Maximum 150s (increased from 120s)

        // Calculate dynamic thresholds
        double baseTimeThreshold = referenceTime * TIME_THRESHOLD_PERCENTAGE;
        double moderateTimeThreshold = Math.max(MIN_TIME_THRESHOLD, Math.min(MAX_TIME_THRESHOLD, baseTimeThreshold));
        double highTimeThreshold = moderateTimeThreshold * 1.8; // 80% higher for HIGH_SUSPICION (was 1.5)

        // Percentage thresholds - VERY conservative to minimize false positives
        // Only flag truly extreme cases that are physically implausible
        final double HIGH_ABSOLUTE_THRESHOLD = 0.30;      // 70% faster than top 3 (was 0.40)
        final double HIGH_INDIVIDUAL_THRESHOLD = 0.30;    // 70% faster than own normal (was 0.40)

        final double MODERATE_ABSOLUTE_THRESHOLD = 0.45;  // 55% faster than top 3 (was 0.60)
        final double MODERATE_INDIVIDUAL_THRESHOLD = 0.45; // 55% faster than own normal (was 0.60)

        // For very short segments (< 50s), require even larger percentage differences
        // because absolute time differences are less meaningful and natural variation is higher
        boolean isShortSegment = referenceTime < 50.0;
        double absoluteMultiplier = isShortSegment ? 0.70 : 1.0; // 30% stricter for short segments (was 25%)

        log.trace("Segment length: {}s, Time thresholds: MODERATE={}s, HIGH={}s, AI={}",
                String.format("%.0f", referenceTime),
                String.format("%.0f", moderateTimeThreshold),
                String.format("%.0f", highTimeThreshold),
                String.format("%.2f", aiValue));

        // SAFETY CHECK: If AI ≈ 1.0 (runner is performing as expected based on their baseline),
        // they're likely running correctly even if the reference time is contaminated by mistakes.
        // This prevents false positives when majority of class made mistakes.
        final double AI_CONSISTENCY_THRESHOLD = 0.85; // AI between 0.85 and 1.15 = consistent
        if (aiValue >= AI_CONSISTENCY_THRESHOLD && aiValue <= (1.0 / AI_CONSISTENCY_THRESHOLD)) {
            log.debug("Runner is consistent with baseline (AI={}), not flagging despite fast PI ({})",
                    String.format("%.2f", aiValue),
                    String.format("%.2f", piReal));
            return AnomalyClassification.NO_SUSPICION;
        }

        // HIGH_SUSPICION requires ALL THREE conditions:
        // 1. Percentage-wise much faster than top-3
        // 2. Percentage-wise much faster than own baseline
        // 3. Absolute time difference is significant (scaled by segment length)
        if (piReal < HIGH_ABSOLUTE_THRESHOLD * absoluteMultiplier
                && aiValue < HIGH_INDIVIDUAL_THRESHOLD * absoluteMultiplier
                && timeDifference >= highTimeThreshold) {
            log.debug("HIGH_SUSPICION detected - piReal: {}, aiValue: {}, timeDiff: {}s, refTime: {}s, thresholds: PI<{}, AI<{}, time>={}s",
                    String.format("%.3f", piReal),
                    String.format("%.3f", aiValue),
                    String.format("%.1f", timeDifference),
                    String.format("%.1f", referenceTime),
                    String.format("%.3f", HIGH_ABSOLUTE_THRESHOLD * absoluteMultiplier),
                    String.format("%.3f", HIGH_INDIVIDUAL_THRESHOLD * absoluteMultiplier),
                    String.format("%.1f", highTimeThreshold));
            return AnomalyClassification.HIGH_SUSPICION;
        }

        // MODERATE_SUSPICION requires ALL THREE conditions
        if (piReal < MODERATE_ABSOLUTE_THRESHOLD * absoluteMultiplier
                && aiValue < MODERATE_INDIVIDUAL_THRESHOLD * absoluteMultiplier
                && timeDifference >= moderateTimeThreshold) {
            log.debug("MODERATE_SUSPICION detected - piReal: {}, aiValue: {}, timeDiff: {}s, refTime: {}s",
                    String.format("%.3f", piReal),
                    String.format("%.3f", aiValue),
                    String.format("%.1f", timeDifference),
                    String.format("%.1f", referenceTime));
            return AnomalyClassification.MODERATE_SUSPICION;
        }

        return AnomalyClassification.NO_SUSPICION;
    }

    @Override
    public AnomalyAnalysis analyzeAnomaly(ResultListId resultListId, List<Long> filterPersonIds) {
        log.debug("Starting mental resilience analysis for result list {} with person filter: {}",
            resultListId, filterPersonIds);

        if (splitTimeListRepository == null || resultListRepository == null || splitTimeAnalysisService == null) {
            return createEmptyAnalysis(resultListId);
        }

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

        // Also create cross-class times for fallback when a class has too few runners
        Map<SegmentKey, List<Double>> allSegmentTimesCrossClass =
            calculateAllTimesPerSegmentCrossClass(splitTimeLists, runtimeMap);

        Function<SplitTimeList, Optional<RunnerAnomalyProfile>> runnerAnalyzer = stl ->
            analyzeRunner(stl, referenceTimesPerSegment, runtimeMap, runnersPerClass,
                allSegmentTimesBySegmentKey, allSegmentTimesCrossClass);

        List<RunnerAnomalyProfile> runnerProfiles = splitTimeLists.stream()
            .filter(personIdFilter)
            .map(runnerAnalyzer)
            .flatMap(Optional::stream)
            .toList();

        log.debug("Analyzed {} runners", runnerProfiles.size());

        return new AnomalyAnalysis(resultListId, eventId, runnerProfiles);
    }

    private AnomalyAnalysis createEmptyAnalysis(ResultListId resultListId) {
        EventId eventId = new EventId(0L); // Placeholder, will be overridden if data exists
        return new AnomalyAnalysis(resultListId, eventId, List.of());
    }

    /**
     * Calculates all times per segment across all classes (class-independent).
     * Used as fallback when a specific class has too few runners.
     */
    private Map<SegmentKey, List<Double>> calculateAllTimesPerSegmentCrossClass(
            List<SplitTimeList> splitTimeLists,
            Map<RuntimeKey, Double> runtimeMap) {

        Map<SegmentKey, List<Double>> result = new HashMap<>();

        if (splitTimeAnalysisService == null) {
            return result;
        }
        for (SplitTimeList splitTimeList : splitTimeLists) {
            List<SegmentTime> segmentTimes = splitTimeAnalysisService.calculateSegmentTimes(
                    splitTimeList, runtimeMap);

            for (SegmentTime segmentTime : segmentTimes) {
                // Use null for className to create cross-class key
                SegmentKey key = new SegmentKey(
                        null,
                        segmentTime.fromControl(),
                        segmentTime.toControl()
                );

                result.computeIfAbsent(key, k -> new ArrayList<>()).add(segmentTime.timeSeconds());
            }
        }

        return result;
    }

    /**
     * Analyzes a single runner and creates their anomaly profile.
     * Returns Optional.empty() if the class has too few runners for reliable analysis.
     */
    private Optional<RunnerAnomalyProfile> analyzeRunner(
        SplitTimeList splitTimeList,
        Map<SegmentKey, Double> referenceTimesPerSegment,
        Map<RuntimeKey, Double> runtimeMap,
        Map<String, Integer> runnersPerClass,
        Map<SegmentKey, List<Double>> allSegmentTimesBySegmentKey,
        Map<SegmentKey, List<Double>> allSegmentTimesCrossClass
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

        if (splitTimeAnalysisService == null) {
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
            analyzeRunnerForAnomaly(className, segmentPIs, normalPI,
                allSegmentTimesBySegmentKey, allSegmentTimesCrossClass);

        if (anomaliesIndexList.isEmpty()) {
            log.debug("No anomalies detected for runner {} in class {}", personId, className);
            return Optional.empty();
        }

        boolean reliableData = classRunnerCount >= RELIABLE_RUNNERS_THRESHOLD;

        Optional<AnomaliesIndex> minAnomaliesIndex =
            anomaliesIndexList.stream().min((x, y) -> Objects.compare(x.aiValue(), y.aiValue(), Double::compare));

        // Create map for quick lookup of actual times by leg number
        Map<Integer, Double> actualTimesByLeg = segmentTimes.stream()
            .collect(java.util.stream.Collectors.toMap(
                SegmentTime::legNumber,
                SegmentTime::timeSeconds
            ));

        return Optional.of(new RunnerAnomalyProfile(
            personId,
            className,
            raceNumber,
            classRunnerCount,
            reliableData,
            normalPI,
            minAnomaliesIndex.get().aiValue(),
            minAnomaliesIndex.get().legNumber(),
            anomaliesIndexList.stream().map(x -> {
                double actualTime = actualTimesByLeg.getOrDefault(x.legNumber(), 0.0);
                // Use the actual cleaned reference time that was used for classification
                double referenceTime = x.cleanedReferenceTime();
                return new AnomaliesIndexInformation(
                    x.legNumber(),
                    ControlCode.of(x.segmentKey().fromControl()),
                    ControlCode.of(x.segmentKey().toControl()),
                    x.piReal(),
                    x,
                    x.classification(),
                    actualTime,
                    referenceTime
                );
            }).toList(),
            minAnomaliesIndex.get().classification()
        ));
    }
}
