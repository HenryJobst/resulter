package de.jobst.resulter.application.analysis;

import de.jobst.resulter.application.port.HangingDetectionService;
import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.application.port.SegmentPI;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.analysis.*;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;

import static de.jobst.resulter.application.analysis.SplitTimeAnalysisServiceImpl.*;

/**
 * Implementation of hanging detection service.
 * Detects cases where runners (passengers) follow faster runners (bus drivers) and
 * achieve better performance than their normal baseline.
 */
@Service
@Slf4j
public class HangingDetectionServiceImpl implements HangingDetectionService {

    // Temporal proximity threshold (30 seconds)
    private static final double TEMPORAL_PROXIMITY_THRESHOLD_SECONDS = 30.0;

    // Performance improvement threshold (0.85 = 15% improvement)
    private static final double HANGING_THRESHOLD = 0.85;

    // Minimum hanging segments for HIGH_HANGING classification
    private static final int MIN_HIGH_HANGING_SEGMENTS = 3;

    // Minimum percentage for HIGH_HANGING classification
    private static final double MIN_HIGH_HANGING_PERCENTAGE = 30.0;

    // Iterative algorithm settings
    private static final int MAX_ITERATIONS = 5;
    private static final double CONVERGENCE_THRESHOLD = 0.01;

    // Reuse existing constants
    private static final int MIN_RUNNERS_PER_CLASS_FOR_ANALYSIS =
            SplitTimeAnalysisServiceImpl.MIN_RUNNERS_PER_CLASS_FOR_ANALYSIS;
    private static final int RELIABLE_RUNNERS_THRESHOLD =
            SplitTimeAnalysisServiceImpl.RELIABLE_RUNNERS_THRESHOLD;

    private final @Nullable SplitTimeListRepository splitTimeListRepository;
    private final @Nullable ResultListRepository resultListRepository;
    private final @Nullable SplitTimeAnalysisServiceImpl splitTimeAnalysisService;

    public HangingDetectionServiceImpl(
            @Nullable SplitTimeListRepository splitTimeListRepository,
            @Nullable ResultListRepository resultListRepository,
            @Nullable SplitTimeAnalysisServiceImpl splitTimeAnalysisService) {
        this.splitTimeListRepository = splitTimeListRepository;
        this.resultListRepository = resultListRepository;
        this.splitTimeAnalysisService = splitTimeAnalysisService;
    }

    @Override
    public HangingAnalysis analyzeHanging(ResultListId resultListId, List<Long> filterPersonIds) {
        log.debug("Starting hanging detection analysis for result list {} with person filter: {}",
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

        // Step 7: Pre-compute all segment PIs for all runners (CRITICAL for performance)
        Map<RunnerSegmentKey, SegmentPIData> allSegmentPIs =
                preComputeAllSegmentPIs(splitTimeLists, referenceTimesPerSegment, runtimeMap);
        log.debug("Pre-computed segment PIs for {} runner-segment combinations", allSegmentPIs.size());

        // Step 8: Build start time map from ResultList
        Map<RunnerKey, PunchTime> startTimeMap = buildStartTimeMap(resultList);
        log.debug("Built start time map for {} runners", startTimeMap.size());

        // Step 9: Build control punch time index
        Map<ControlKey, List<PunchTimeRecord>> controlPunchTimes =
                buildControlPunchTimeIndex(splitTimeLists);
        log.debug("Built punch time index for {} controls", controlPunchTimes.size());

        // Step 10: Analyze each runner
        Set<Long> filterPersonIdSet = new HashSet<>(filterPersonIds);
        Predicate<SplitTimeList> personIdFilter = stl ->
                filterPersonIdSet.isEmpty() || filterPersonIdSet.contains(stl.getPersonId().value());

        List<RunnerHangingProfile> runnerProfiles = splitTimeLists.stream()
                .filter(personIdFilter)
                .map(stl -> analyzeRunner(
                        stl,
                        referenceTimesPerSegment,
                        runtimeMap,
                        runnersPerClass,
                        allSegmentPIs,
                        controlPunchTimes,
                        startTimeMap
                ))
                .flatMap(Optional::stream)
                // Don't filter - return ALL runners so frontend can build complete startPositionMap
                .toList();

        long runnersWithHanging = runnerProfiles.stream()
                .filter(RunnerHangingProfile::hasHanging)
                .count();
        log.debug("Analyzed {} runners, {} with hanging behavior", splitTimeLists.size(), runnersWithHanging);

        // Step 10: Calculate statistics
        HangingStatistics stats = calculateStatistics(splitTimeLists.size(), runnerProfiles);

        return new HangingAnalysis(resultListId, eventId, runnerProfiles, stats);
    }

    /**
     * Pre-computes all segment PIs for all runners.
     * This is critical for performance when checking if bus drivers were faster.
     */
    private Map<RunnerSegmentKey, SegmentPIData> preComputeAllSegmentPIs(
            List<SplitTimeList> splitTimeLists,
            Map<SegmentKey, Double> referenceTimesPerSegment,
            Map<RuntimeKey, Double> runtimeMap) {

        Map<RunnerSegmentKey, SegmentPIData> cache = new HashMap<>();

        for (SplitTimeList stl : splitTimeLists) {
            PersonId personId = stl.getPersonId();
            String className = stl.getClassResultShortName().value();
            RaceNumber raceNumber = stl.getRaceNumber();

            List<SegmentTime> segmentTimes = splitTimeAnalysisService.calculateSegmentTimes(stl, runtimeMap);
            List<SegmentPI> segmentPIs = splitTimeAnalysisService.calculateSegmentPIs(
                    segmentTimes, referenceTimesPerSegment, className);

            for (SegmentPI segPI : segmentPIs) {
                RunnerSegmentKey key = new RunnerSegmentKey(
                        personId,
                        className,
                        raceNumber,
                        segPI.legNumber(),
                        segPI.fromControl(),
                        segPI.toControl()
                );

                cache.put(key, new SegmentPIData(segPI.pi(), segPI.runnerTime()));
            }
        }

        return cache;
    }

    /**
     * Builds an index of punch times at each control for all runners.
     * Used to find potential bus drivers (runners who punched shortly before).
     */
    private Map<ControlKey, List<PunchTimeRecord>> buildControlPunchTimeIndex(
            List<SplitTimeList> splitTimeLists) {

        Map<ControlKey, List<PunchTimeRecord>> index = new HashMap<>();

        for (SplitTimeList stl : splitTimeLists) {
            PersonId personId = stl.getPersonId();
            String className = stl.getClassResultShortName().value();
            RaceNumber raceNumber = stl.getRaceNumber();

            for (SplitTime split : stl.getSplitTimes()) {
                if (split.getPunchTime().value() == null) continue;

                // Index by control code (across all classes for cross-class hanging)
                ControlKey key = new ControlKey(split.getControlCode().value());

                PunchTimeRecord record = new PunchTimeRecord(
                        personId,
                        className,
                        raceNumber,
                        split.getPunchTime().value()
                );

                index.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
            }
        }

        // Sort each control's punch times for efficient searching
        index.values().forEach(list ->
                list.sort(Comparator.comparing(PunchTimeRecord::punchTime))
        );

        return index;
    }

    /**
     * Analyzes a single runner for hanging behavior.
     */
    private Optional<RunnerHangingProfile> analyzeRunner(
            SplitTimeList splitTimeList,
            Map<SegmentKey, Double> referenceTimesPerSegment,
            Map<RuntimeKey, Double> runtimeMap,
            Map<String, Integer> runnersPerClass,
            Map<RunnerSegmentKey, SegmentPIData> allSegmentPIs,
            Map<ControlKey, List<PunchTimeRecord>> controlPunchTimes,
            Map<RunnerKey, PunchTime> startTimeMap) {

        PersonId personId = splitTimeList.getPersonId();
        String className = splitTimeList.getClassResultShortName().value();
        RaceNumber raceNumber = splitTimeList.getRaceNumber();

        // Check minimum runner threshold
        int classRunnerCount = runnersPerClass.getOrDefault(className, 0);
        if (classRunnerCount < MIN_RUNNERS_PER_CLASS_FOR_ANALYSIS) {
            log.info("Skipping hanging analysis for runner {} - only {} runners",
                    personId, classRunnerCount);
            return Optional.empty();
        }

        // Calculate segment times and PIs
        List<SegmentTime> segmentTimes =
                splitTimeAnalysisService.calculateSegmentTimes(splitTimeList, runtimeMap);

        if (segmentTimes.isEmpty()) {
            return Optional.empty();
        }

        List<SegmentPI> segmentPIs =
                splitTimeAnalysisService.calculateSegmentPIs(
                        segmentTimes, referenceTimesPerSegment, className);

        // Phase 1: Calculate initial Normal PI (uses median internally, robust against outliers)
        PerformanceIndex normalPI = calculateNormalPI(segmentPIs);

        if (normalPI == null) {
            log.info("Skipping hanging analysis - insufficient non-mistake segments");
            return Optional.empty();
        }

        // Build segment-to-punch-time map for this runner
        Map<String, Double> passengerPunchTimes = buildPunchTimeMap(splitTimeList);

        // Phase 2: Iterative refinement to remove circular dependency
        List<HangingPair> hangingPairs = null;

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            // Detect hanging pairs with current Normal PI
            hangingPairs = detectHangingPairs(
                    segmentPIs,
                    normalPI,
                    passengerPunchTimes,
                    controlPunchTimes,
                    allSegmentPIs,
                    personId);

            // If no hanging detected, we're done
            if (hangingPairs.isEmpty()) {
                log.debug("Iteration {}: No hanging pairs detected", iteration);
                break;
            }

            // Filter out hanging segments from baseline calculation
            final List<HangingPair> currentHangingPairs = hangingPairs;
            List<SegmentPI> cleanSegments = segmentPIs.stream()
                    .filter(seg -> !isHangingSegment(seg, currentHangingPairs))
                    .toList();

            // Recalculate Normal PI without hanging segments
            PerformanceIndex newNormalPI = calculateNormalPI(cleanSegments);

            if (newNormalPI == null) {
                // Not enough clean segments left - use previous result
                log.debug("Iteration {}: Insufficient clean segments, reverting to previous", iteration);
                break;
            }

            // Check for convergence
            double diff = Math.abs(newNormalPI.value() - normalPI.value());
            log.debug("Iteration {}: Normal PI = {}, diff = {}, hanging pairs = {}",
                    iteration, String.format("%.3f", newNormalPI.value()),
                    String.format("%.3f", diff), hangingPairs.size());

            if (diff < CONVERGENCE_THRESHOLD) {
                log.debug("Converged after {} iterations", iteration + 1);
                normalPI = newNormalPI;
                break;
            }

            // Update for next iteration
            normalPI = newNormalPI;
        }

        // Initialize empty list if no hanging detected
        if (hangingPairs == null) {
            hangingPairs = List.of();
        }

        // Calculate average HI (1.0 if no hanging pairs)
        double averageHI = hangingPairs.isEmpty()
                ? 1.0
                : hangingPairs.stream()
                        .mapToDouble(p -> p.hangingIndex().value())
                        .average()
                        .orElse(1.0);

        // Count total non-mistake segments (for percentage calculation)
        int totalNonMistakeSegments = (int) segmentPIs.stream()
                .filter(seg -> !seg.toControl().equals(FINAL_CODE))
                .filter(seg -> !seg.fromControl().equals(START_CODE))
                .count();

        // Classify
        HangingClassification classification = classifyHanging(
                hangingPairs.size(),
                totalNonMistakeSegments
        );

        boolean reliableData = classRunnerCount >= RELIABLE_RUNNERS_THRESHOLD;

        // Get start time from ResultList data (PersonRaceResult.startTime)
        RunnerKey runnerKey = new RunnerKey(personId, className, raceNumber);
        PunchTime startTime = startTimeMap.getOrDefault(runnerKey, PunchTime.of(null));

        return Optional.of(new RunnerHangingProfile(
                personId,
                className,
                raceNumber,
                startTime,
                classRunnerCount,
                reliableData,
                normalPI,
                hangingPairs,
                averageHI,
                classification,
                totalNonMistakeSegments
        ));
    }

    /**
     * Builds a map of control codes to punch times for a runner.
     */
    private Map<String, Double> buildPunchTimeMap(SplitTimeList splitTimeList) {
        Map<String, Double> map = new HashMap<>();

        for (SplitTime split : splitTimeList.getSplitTimes()) {
            if (split.getPunchTime().value() != null) {
                map.put(split.getControlCode().value(), split.getPunchTime().value());
            }
        }

        return map;
    }

    /**
     * Detects hanging pairs for a runner on each segment.
     */
    private List<HangingPair> detectHangingPairs(
            List<SegmentPI> segmentPIs,
            PerformanceIndex normalPI,
            Map<String, Double> passengerPunchTimes,
            Map<ControlKey, List<PunchTimeRecord>> controlPunchTimes,
            Map<RunnerSegmentKey, SegmentPIData> allSegmentPIs,
            PersonId passengerId) {

        List<HangingPair> hangingPairs = new ArrayList<>();

        for (SegmentPI segmentPI : segmentPIs) {
            // Skip first and final segments (same as anomaly detection)
            if (segmentPI.fromControl().equals(START_CODE)) {
                continue;
            }
            if (segmentPI.toControl().equals(FINAL_CODE)) {
                continue;
            }

            // Check Criterion C: Performance Improvement (Passenger Threshold)
            HangingIndex hi = HangingIndex.of(segmentPI.pi(), normalPI);
            if (!hi.isHanging(HANGING_THRESHOLD)) {
                continue; // Not enough improvement
            }

            // Get passenger's punch time at control B (toControl of segment)
            // Note: We need the punch time at the END of the segment (toControl) to see who arrived first
            Double passengerPunchTime = passengerPunchTimes.get(segmentPI.toControl());
            if (passengerPunchTime == null) {
                continue;
            }

            // Find potential bus drivers at this control
            ControlKey controlKey = new ControlKey(segmentPI.toControl());
            List<PunchTimeRecord> punchRecords =
                    controlPunchTimes.getOrDefault(controlKey, List.of());

            // Find bus drivers: punched within 30s before passenger
            List<BusDriverCandidate> busDrivers = findBusDrivers(
                    punchRecords,
                    passengerPunchTime,
                    passengerId, segmentPI,
                    allSegmentPIs
            );

            // Create a hanging pair for EACH bus driver candidate
            for (BusDriverCandidate driver : busDrivers) {
                HangingPair pair = new HangingPair(
                        segmentPI.legNumber(),
                        ControlCode.of(segmentPI.fromControl()),
                        ControlCode.of(segmentPI.toControl()),
                        driver.personId(),
                        driver.className(),
                        driver.raceNumber(),
                        passengerPunchTime - driver.punchTime(), // time delta
                        segmentPI.pi(), // passenger PI
                        driver.busDriverPI(),
                        hi,
                        segmentPI.runnerTime(), // passenger actual time
                        driver.busDriverActualTime(),
                        segmentPI.referenceTime()
                );

                hangingPairs.add(pair);

                log.debug("Hanging detected: Leg {}, Passenger {} followed Driver {} " +
                                "(ΔT={}s, HI={}, PassengerPI={}, DriverPI={})",
                        segmentPI.legNumber(),
                        passengerId,
                        driver.personId(),
                        String.format("%.1f", pair.timeDeltaSeconds()),
                        String.format("%.3f", hi.value()),
                        String.format("%.3f", segmentPI.pi().value()),
                        String.format("%.3f", driver.busDriverPI().value())
                );
            }
        }

        return hangingPairs;
    }

    /**
     * Finds all bus drivers from punch records at a control.
     * Returns ALL runners who:
     * - Punched within 30s before the passenger
     * - Were faster on this segment (lower PI)
     * - Are not the passenger themselves
     */
    private List<BusDriverCandidate> findBusDrivers(
            List<PunchTimeRecord> punchRecords,
            double passengerPunchTime,
            PersonId passengerId, SegmentPI passengerSegmentPI,
            Map<RunnerSegmentKey, SegmentPIData> allSegmentPIs) {

        List<BusDriverCandidate> result = new ArrayList<>();

        // Filter candidates: punched before passenger, within 30s window
        List<PunchTimeRecord> candidates = punchRecords.stream()
                .filter(r -> r.punchTime() < passengerPunchTime) // Criterion A.1
                .filter(r -> passengerPunchTime - r.punchTime() <=
                        TEMPORAL_PROXIMITY_THRESHOLD_SECONDS) // Criterion A.2
                .filter(r -> !r.personId().equals(passengerId)) // Not self
                .toList();

        if (candidates.isEmpty()) {
            return result;
        }

        // Check each candidate if they were faster on this segment
        for (PunchTimeRecord candidate : candidates) {
            RunnerSegmentKey busDriverKey = new RunnerSegmentKey(
                    candidate.personId(),
                    candidate.className(),
                    candidate.raceNumber(),
                    passengerSegmentPI.legNumber(),
                    passengerSegmentPI.fromControl(),
                    passengerSegmentPI.toControl()
            );

            SegmentPIData busDriverData = allSegmentPIs.get(busDriverKey);

            if (busDriverData != null &&
                    busDriverData.pi().value() < passengerSegmentPI.pi().value()) {
                // Criterion B: Bus driver was faster
                result.add(new BusDriverCandidate(
                        candidate.personId(),
                        candidate.className(),
                        candidate.raceNumber(),
                        candidate.punchTime(),
                        busDriverData.pi(),
                        busDriverData.actualTime()
                ));
            }
        }

        return result;
    }

    /**
     * Classifies hanging behavior based on count and percentage.
     */
    private HangingClassification classifyHanging(int hangingCount, int totalSegments) {
        if (totalSegments == 0) {
            return HangingClassification.INSUFFICIENT_DATA;
        }

        double percentage = (double) hangingCount / totalSegments * 100.0;

        // HIGH_HANGING: ≥3 segments OR ≥30% of segments
        if (hangingCount >= MIN_HIGH_HANGING_SEGMENTS ||
                percentage >= MIN_HIGH_HANGING_PERCENTAGE) {
            return HangingClassification.HIGH_HANGING;
        }

        // MODERATE_HANGING: 1-2 segments OR 10-29%
        if (hangingCount > 0) {
            return HangingClassification.MODERATE_HANGING;
        }

        return HangingClassification.NO_HANGING;
    }

    /**
     * Calculates aggregate statistics for the analysis.
     */
    private HangingStatistics calculateStatistics(int totalRunners, List<RunnerHangingProfile> runnerProfiles) {
        // Count only runners with actual hanging behavior (not NO_HANGING or INSUFFICIENT_DATA)
        int runnersWithHanging = (int) runnerProfiles.stream()
                .filter(RunnerHangingProfile::hasHanging)
                .count();

        int totalHangingSegments = runnerProfiles.stream()
                .mapToInt(RunnerHangingProfile::getHangingCount)
                .sum();

        int highHangingRunners = (int) runnerProfiles.stream()
                .filter(p -> p.classification() == HangingClassification.HIGH_HANGING)
                .count();

        int moderateHangingRunners = (int) runnerProfiles.stream()
                .filter(p -> p.classification() == HangingClassification.MODERATE_HANGING)
                .count();

        // Calculate average and median hanging index across all hanging pairs
        List<Double> allHIs = runnerProfiles.stream()
                .flatMap(p -> p.hangingPairs().stream())
                .map(pair -> pair.hangingIndex().value())
                .toList();

        Double averageHI = allHIs.isEmpty() ? null :
                allHIs.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        Double medianHI = allHIs.isEmpty() ? null : calculateMedian(allHIs);

        return new HangingStatistics(
                totalRunners,
                runnersWithHanging,
                totalHangingSegments,
                highHangingRunners,
                moderateHangingRunners,
                averageHI,
                medianHI
        );
    }

    /**
     * Calculates the median of a list of doubles.
     */
    private double calculateMedian(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0;
        }

        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);

        int size = sorted.size();
        if (size % 2 == 0) {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        } else {
            return sorted.get(size / 2);
        }
    }

    /**
     * Calculates Normal PI from filtered segments.
     * Wrapper around splitTimeAnalysisService.calculateNormalPI() to allow filtering.
     */
    private @Nullable PerformanceIndex calculateNormalPI(List<SegmentPI> segmentPIs) {
        return splitTimeAnalysisService.calculateNormalPI(segmentPIs);
    }

    /**
     * Checks if a segment is a hanging segment based on detected hanging pairs.
     */
    private boolean isHangingSegment(SegmentPI segment, List<HangingPair> hangingPairs) {
        return hangingPairs.stream()
                .anyMatch(pair ->
                        pair.legNumber() == segment.legNumber() &&
                        pair.fromControl().value().equals(segment.fromControl()) &&
                        pair.toControl().value().equals(segment.toControl())
                );
    }

    private HangingAnalysis createEmptyAnalysis(ResultListId resultListId) {
        EventId eventId = new EventId(0L); // Placeholder
        HangingStatistics emptyStats = new HangingStatistics(0, 0, 0, 0, 0, null, null);
        return new HangingAnalysis(resultListId, eventId, List.of(), emptyStats);
    }

    // Helper records

    /**
     * Key for looking up a runner's segment PI in the pre-computed cache.
     */
    private record RunnerSegmentKey(
            PersonId personId,
            String className,
            RaceNumber raceNumber,
            int legNumber,
            String fromControl,
            String toControl
    ) {}

    /**
     * Cached segment PI data for a runner on a specific segment.
     */
    private record SegmentPIData(
            PerformanceIndex pi,
            double actualTime
    ) {}

    /**
     * Key for control punch time index.
     */
    private record ControlKey(String controlCode) {}

    /**
     * Punch time record for a runner at a control.
     */
    private record PunchTimeRecord(
            PersonId personId,
            String className,
            RaceNumber raceNumber,
            double punchTime
    ) {}

    /**
     * Bus driver candidate with their segment performance data.
     */
    private record BusDriverCandidate(
            PersonId personId,
            String className,
            RaceNumber raceNumber,
            double punchTime,
            PerformanceIndex busDriverPI,
            double busDriverActualTime
    ) {}

    /**
     * Key for looking up a runner's start time.
     */
    private record RunnerKey(
            PersonId personId,
            String className,
            RaceNumber raceNumber
    ) {}

    /**
     * Builds a map of start times from ResultList data.
     * Extracts startTime from PersonRaceResult for each runner.
     */
    private Map<RunnerKey, PunchTime> buildStartTimeMap(ResultList resultList) {
        Map<RunnerKey, PunchTime> startTimeMap = new HashMap<>();

        if (resultList.getClassResults() == null) {
            return startTimeMap;
        }

        // Navigate: ResultList → ClassResults → PersonResults → PersonRaceResults
        for (ClassResult classResult : resultList.getClassResults()) {
            String className = classResult.classResultShortName().value();

            for (PersonResult personResult : classResult.personResults().value()) {
                PersonId personId = personResult.personId();

                for (PersonRaceResult personRaceResult : personResult.personRaceResults().value()) {
                    RaceNumber raceNumber = personRaceResult.getRaceNumber();

                    // Extract startTime (DateTime wraps ZonedDateTime)
                    DateTime startTime = personRaceResult.getStartTime();

                    // Convert ZonedDateTime to epoch seconds (Double) for PunchTime
                    Double startTimeSeconds = null;
                    if (startTime != null && startTime.value() != null) {
                        startTimeSeconds = (double) startTime.value().toEpochSecond();
                    }

                    RunnerKey key = new RunnerKey(personId, className, raceNumber);
                    startTimeMap.put(key, PunchTime.of(startTimeSeconds));

                    if (startTimeSeconds != null) {
                        log.trace("Start time for runner {} ({}): {} seconds",
                                personId, className, String.format("%.0f", startTimeSeconds));
                    }
                }
            }
        }

        return startTimeMap;
    }
}
