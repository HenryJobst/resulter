package de.jobst.resulter.application;

import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.analysis.*;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Implementation of Mental Resilience Index analysis service.
 *
 * <p>Calculates how runners mentally react after making navigation mistakes by analyzing
 * their pace changes on segments following errors.</p>
 */
@Service
@Slf4j
public class MentalResilienceServiceImpl implements MentalResilienceService {

    /**
     * Minimum number of runners required in a class for MRI calculation.
     * Below this threshold, statistical validity is too low.
     */
    private static final int MIN_RUNNERS_FOR_MRI = 3;

    /**
     * Threshold for reliable MRI data.
     * Classes with fewer runners will get a warning indicator.
     */
    private static final int RELIABLE_RUNNERS_THRESHOLD = 5;

    /**
     * Minimum number of non-mistake segments required to calculate Normal PI.
     * A runner needs at least this many "good" segments to establish a baseline.
     */
    private static final int MIN_NON_MISTAKE_SEGMENTS = 3;

    /**
     * Relative mistake threshold in percent (Winsplits default: 25%).
     * A segment is considered a mistake if the time loss percentage exceeds
     * the normalized difference + this threshold.
     */
    private static final double RELATIVE_MISTAKE_THRESHOLD_PERCENT = 25.0;

    /**
     * Absolute time loss threshold in seconds (Winsplits default: 30s).
     * A segment is only considered a mistake if BOTH the relative and absolute
     * thresholds are exceeded.
     */
    private static final double ABSOLUTE_MISTAKE_THRESHOLD_SECONDS = 30.0;

    private final SplitTimeListRepository splitTimeListRepository;
    private final ResultListRepository resultListRepository;

    public MentalResilienceServiceImpl(
            SplitTimeListRepository splitTimeListRepository, ResultListRepository resultListRepository) {
        this.splitTimeListRepository = splitTimeListRepository;
        this.resultListRepository = resultListRepository;
    }

    @Override
    public MentalResilienceAnalysis analyzeMentalResilience(
            ResultListId resultListId,
            List<Long> filterPersonIds) {

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
        Map<String, Double> runtimeMap = buildRuntimeMap(resultList);
        log.debug("Built runtime map with {} entries", runtimeMap.size());

        // Step 5: Apply person filter if provided
        Set<Long> filterPersonIdSet = !filterPersonIds.isEmpty()
                ? new HashSet<>(filterPersonIds)
                : Collections.emptySet();

        if (!filterPersonIdSet.isEmpty()) {
            splitTimeLists = splitTimeLists.stream()
                    .filter(stl -> filterPersonIdSet.contains(stl.getPersonId().value()))
                    .toList();
            log.debug("After person filter: {} split time lists", splitTimeLists.size());
        }

        // Step 6: Count runners per class
        Map<String, Integer> runnersPerClass = countRunnersPerClass(splitTimeLists);
        log.debug("Runner count per class: {}", runnersPerClass);

        // Step 7: Calculate best times per segment
        Map<String, Double> bestTimesPerSegment = calculateBestTimesPerSegment(splitTimeLists, runtimeMap);
        log.debug("Calculated best times for {} segments", bestTimesPerSegment.size());

        // Step 8: Analyze each runner
        List<RunnerMentalProfile> runnerProfiles = new ArrayList<>();
        for (SplitTimeList splitTimeList : splitTimeLists) {
            try {
                RunnerMentalProfile profile = analyzeRunner(splitTimeList, bestTimesPerSegment, runtimeMap, runnersPerClass);
                if (profile != null && profile.hasMistakes()) {
                    runnerProfiles.add(profile);
                }
            } catch (Exception e) {
                log.warn("Failed to analyze runner {}: {}", splitTimeList.getPersonId(), e.getMessage());
            }
        }

        log.debug("Analyzed {} runners with mistakes", runnerProfiles.size());

        // Step 8: Calculate aggregate statistics
        MriStatistics statistics = calculateStatistics(splitTimeLists.size(), runnerProfiles);

        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("Mental resilience analysis completed in {} ms: {} runners, {} with mistakes, {} total mistakes",
                elapsedTime, statistics.totalRunners(), statistics.runnersWithMistakes(), statistics.totalMistakes());

        return new MentalResilienceAnalysis(resultListId, eventId, runnerProfiles, statistics);
    }

    /**
     * Builds a map of runtime values keyed by "personId-className-raceNumber".
     */
    private Map<String, Double> buildRuntimeMap(ResultList resultList) {
        Map<String, Double> runtimeMap = new HashMap<>();

        if (resultList.getClassResults() == null) {
            return runtimeMap;
        }
        resultList.getClassResults().forEach(classResult -> classResult.personResults()
            .value()
            .stream()
            .flatMap(personResult -> personResult.personRaceResults().value().stream())
            .forEach(raceResult -> {
                PunchTime runtime = raceResult.getRuntime();
                if (runtime.value() != null && runtime.value() > 0) {
                    runtimeMap.put(buildRuntimeKey(raceResult.getPersonId().value(),
                        classResult.classResultShortName().value(),
                        raceResult.getRaceNumber().value().intValue()), runtime.value());
                }
            }));

        return runtimeMap;
    }

    private String buildRuntimeKey(Long personId, String className, Integer raceNumber) {
        return personId + "-" + className + "-" + raceNumber;
    }

    /**
     * Counts the number of runners per class.
     */
    private Map<String, Integer> countRunnersPerClass(List<SplitTimeList> splitTimeLists) {
        Map<String, Integer> counts = new HashMap<>();
        for (SplitTimeList splitTimeList : splitTimeLists) {
            String className = splitTimeList.getClassResultShortName().value();
            counts.put(className, counts.getOrDefault(className, 0) + 1);
        }
        return counts;
    }

    /**
     * Number of top runners to use for reference time calculation.
     * Using top 3 instead of best time alone provides robustness against outliers.
     */
    private static final int TOP_RUNNERS_FOR_REFERENCE = 3;

    /**
     * Calculates reference time for each segment per class.
     * Uses average of top 3 times (instead of single best time) to avoid outliers.
     * This approach is more robust and matches tools like Winsplits.
     * Each class has its own reference times to ensure fair comparison within the same age/skill group.
     */
    private Map<String, Double> calculateBestTimesPerSegment(
            List<SplitTimeList> splitTimeLists,
            Map<String, Double> runtimeMap) {

        // Map: "className-fromControl-toControl" -> list of segment times
        Map<String, List<Double>> segmentTimesMap = new HashMap<>();

        for (SplitTimeList splitTimeList : splitTimeLists) {
            String className = splitTimeList.getClassResultShortName().value();
            List<SegmentTime> segmentTimes = calculateSegmentTimes(splitTimeList, runtimeMap);

            for (SegmentTime segmentTime : segmentTimes) {
                // Include className in key to calculate reference times per class
                String segmentKey = className + "-" + segmentTime.fromControl + "-" + segmentTime.toControl;
                segmentTimesMap.computeIfAbsent(segmentKey, k -> new ArrayList<>())
                        .add(segmentTime.timeSeconds);
            }
        }

        // Calculate reference time as average of top 3 times for each segment per class
        Map<String, Double> bestTimes = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : segmentTimesMap.entrySet()) {
            List<Double> times = entry.getValue();

            if (times.isEmpty()) {
                continue;
            }

            // Sort times ascending
            List<Double> sortedTimes = new ArrayList<>(times);
            Collections.sort(sortedTimes);

            // Take average of top N times (or all if fewer runners)
            int topN = Math.min(TOP_RUNNERS_FOR_REFERENCE, sortedTimes.size());
            double referenceTime = sortedTimes.stream()
                    .limit(topN)
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            if (referenceTime > 0) {
                bestTimes.put(entry.getKey(), referenceTime);
                log.trace("Segment {}: reference time = {:.1f}s (avg of top {} from {} runners)",
                        entry.getKey(), referenceTime, topN, times.size());
            }
        }

        return bestTimes;
    }

    /**
     * Analyzes a single runner and creates their mental profile.
     * Returns null if the class has too few runners for reliable analysis.
     */
    private @Nullable RunnerMentalProfile analyzeRunner(
            SplitTimeList splitTimeList,
            Map<String, Double> bestTimesPerSegment,
            Map<String, Double> runtimeMap,
            Map<String, Integer> runnersPerClass) {

        PersonId personId = splitTimeList.getPersonId();
        String className = splitTimeList.getClassResultShortName().value();
        RaceNumber raceNumber = splitTimeList.getRaceNumber();

        // Check minimum runner threshold
        int classRunnerCount = runnersPerClass.getOrDefault(className, 0);
        if (classRunnerCount < MIN_RUNNERS_FOR_MRI) {
            log.info("Skipping MRI for runner {} in class {} - only {} runners (min: {})",
                    personId, className, classRunnerCount, MIN_RUNNERS_FOR_MRI);
            return null;
        }

        // Calculate segment times
        List<SegmentTime> segmentTimes = calculateSegmentTimes(splitTimeList, runtimeMap);

        if (segmentTimes.isEmpty()) {
            log.debug("No valid segments for runner {}", personId);
            return null;
        }

        // Calculate Performance Index for each segment
        List<SegmentPI> segmentPIs = calculateSegmentPIs(segmentTimes, bestTimesPerSegment, className);

        // Calculate Normal PI (average excluding mistakes)
        PerformanceIndex normalPI = calculateNormalPI(segmentPIs);

        // Check if we have enough non-mistake segments to establish a baseline
        if (normalPI == null) {
            log.info("Skipping MRI for runner {} in class {} - too many mistakes (< {} non-mistake segments)",
                    personId, className, MIN_NON_MISTAKE_SEGMENTS);
            return null;
        }

        // Convert Normal-PI back to median difference % for Winsplits-style threshold calculation
        double medianDifferencePercent = (normalPI.value() - 1.0) * 100.0;

        // Detect mistakes and calculate reactions using Winsplits criteria
        List<MistakeReactionPair> mistakeReactions = detectMistakesAndReactions(
                segmentPIs,
                normalPI,
                medianDifferencePercent);

        if (mistakeReactions.isEmpty()) {
            log.debug("No mistakes detected for runner {} in class {}", personId, className);
            return null;
        }

        // Calculate average MRI
        double averageMRI = mistakeReactions.stream()
                .mapToDouble(pair -> pair.mri().value())
                .average()
                .orElse(0.0);

        // Classify based on average MRI
        MentalClassification classification = MentalResilienceIndex.of(
                new PerformanceIndex(normalPI.value() + averageMRI),
                normalPI
        ).classify();

        boolean reliableData = classRunnerCount >= RELIABLE_RUNNERS_THRESHOLD;

        return new RunnerMentalProfile(
                personId,
                className,
                raceNumber,
                classRunnerCount,
                reliableData,
                normalPI,
                mistakeReactions,
                averageMRI,
                classification
        );
    }

    /**
     * Calculates segment times between consecutive controls.
     */
    private List<SegmentTime> calculateSegmentTimes(
            SplitTimeList splitTimeList,
            Map<String, Double> runtimeMap) {

        List<SegmentTime> segmentTimes = new ArrayList<>();

        // Add virtual start control at 0.0 seconds
        List<SplitTime> allSplits = new ArrayList<>();
        allSplits.add(new SplitTime(ControlCode.of("S"), PunchTime.of(0.0), null));
        allSplits.addAll(splitTimeList.getSplitTimes());

        // Add virtual finish control with runtime
        String runtimeKey = buildRuntimeKey(
                splitTimeList.getPersonId().value(),
                splitTimeList.getClassResultShortName().value(),
                splitTimeList.getRaceNumber().value().intValue()
        );
        Double runtime = runtimeMap.get(runtimeKey);
        if (runtime != null && runtime > 0) {
            allSplits.add(new SplitTime(ControlCode.of("F"), PunchTime.of(runtime), null));
        }

        // Sort by punch time
        allSplits.sort(Comparator.comparing(st ->
            st.getPunchTime().value() != null
                ? st.getPunchTime().value()
                : Double.MAX_VALUE));

        // Calculate segment times
        for (int i = 0; i < allSplits.size() - 1; i++) {
            SplitTime current = allSplits.get(i);
            SplitTime next = allSplits.get(i + 1);

            if (current.getPunchTime().value() != null && next.getPunchTime().value() != null) {
                double segmentTime = next.getPunchTime().value() - current.getPunchTime().value();
                if (segmentTime > 0) {
                    segmentTimes.add(new SegmentTime(
                            i,
                            current.getControlCode().value(),
                            next.getControlCode().value(),
                            segmentTime
                    ));
                }
            }
        }

        return segmentTimes;
    }

    /**
     * Calculates Performance Index for each segment.
     * Uses class-specific best times for fair comparison within the same age/skill group.
     */
    private List<SegmentPI> calculateSegmentPIs(
            List<SegmentTime> segmentTimes,
            Map<String, Double> bestTimesPerSegment,
            String className) {

        List<SegmentPI> segmentPIs = new ArrayList<>();

        for (SegmentTime segmentTime : segmentTimes) {
            // Include className to lookup class-specific best time
            String segmentKey = className + "-" + segmentTime.fromControl + "-" + segmentTime.toControl;
            Double bestTime = bestTimesPerSegment.get(segmentKey);

            if (bestTime != null && bestTime > 0) {
                PerformanceIndex pi = PerformanceIndex.of(segmentTime.timeSeconds, bestTime);
                segmentPIs.add(new SegmentPI(
                        segmentTime.legNumber,
                        segmentTime.fromControl,
                        segmentTime.toControl,
                        segmentTime.timeSeconds,
                        bestTime,
                        pi
                ));
            }
        }

        return segmentPIs;
    }

    /**
     * Calculates Normal PI using Winsplits approach.
     *
     * <p>Winsplits method:</p>
     * <ol>
     *   <li>Calculate time difference percentage for each segment: (runnerTime - refTime) / refTime * 100</li>
     *   <li>Find median of these differences = "normalized performance"</li>
     *   <li>Filter mistakes: diff% > median% + 25% AND time loss > 30s</li>
     *   <li>Recalculate median from non-mistake segments</li>
     * </ol>
     *
     * <p>Note: PI = runnerTime/refTime, so diff% = (PI - 1) * 100</p>
     *
     * @param segmentPIs All segment PIs for the runner
     * @return Normal PI (converted back from median difference %) or null if too few valid segments
     */
    private @Nullable PerformanceIndex calculateNormalPI(List<SegmentPI> segmentPIs) {
        if (segmentPIs.size() < MIN_NON_MISTAKE_SEGMENTS) {
            log.debug("Not enough segments ({}) to calculate Normal PI (min: {})",
                    segmentPIs.size(), MIN_NON_MISTAKE_SEGMENTS);
            return null;
        }

        // Calculate time difference percentages for all segments
        // diff% = (PI - 1) * 100
        List<Double> allDifferencePercents = segmentPIs.stream()
                .map(seg -> (seg.pi().value() - 1.0) * 100.0)
                .toList();

        // Calculate preliminary median difference (Winsplits "normalized performance")
        Double medianDifferencePercent = calculateMedian(allDifferencePercents);
        if (medianDifferencePercent == null) {
            log.warn("Failed to calculate median difference percent");
            return null;
        }

        log.debug("Preliminary median difference = {}%", String.format("%.1f", medianDifferencePercent));

        // Filter out mistakes using Winsplits criteria:
        // 1. Time loss % > median% + RELATIVE_MISTAKE_THRESHOLD_PERCENT (default 25%)
        // 2. Absolute time loss > ABSOLUTE_MISTAKE_THRESHOLD_SECONDS (default 30s)
        List<Double> nonMistakeDifferences = new ArrayList<>();
        for (int i = 0; i < segmentPIs.size(); i++) {
            SegmentPI seg = segmentPIs.get(i);
            double diffPercent = allDifferencePercents.get(i);
            double timeLossSeconds = seg.runnerTime() - seg.bestTime();

            boolean isMistake = diffPercent > (medianDifferencePercent + RELATIVE_MISTAKE_THRESHOLD_PERCENT)
                    && timeLossSeconds > ABSOLUTE_MISTAKE_THRESHOLD_SECONDS;

            if (!isMistake) {
                nonMistakeDifferences.add(diffPercent);
            } else {
                log.trace("Leg {}: Mistake detected - diff={}%, time loss={}s",
                        seg.legNumber(), String.format("%.1f", diffPercent), String.format("%.1f", timeLossSeconds));
            }
        }

        // Check if we have enough non-mistake segments
        if (nonMistakeDifferences.size() < MIN_NON_MISTAKE_SEGMENTS) {
            log.info("Not enough non-mistake segments ({}) after filtering (min: {})",
                    nonMistakeDifferences.size(), MIN_NON_MISTAKE_SEGMENTS);
            return null;
        }

        // Calculate final median difference from non-mistake segments
        Double finalMedianDifferencePercent = calculateMedian(nonMistakeDifferences);
        if (finalMedianDifferencePercent == null) {
            log.warn("Failed to calculate final median difference despite having {} segments", nonMistakeDifferences.size());
            return null;
        }

        // Convert median difference% back to PI: PI = 1 + (diff% / 100)
        double normalPI = 1.0 + (finalMedianDifferencePercent / 100.0);

        log.debug("Calculated Normal PI = {} (median diff = {}%) from {} non-mistake segments (out of {} total)",
                String.format("%.3f", normalPI),
                String.format("%.1f", finalMedianDifferencePercent),
                nonMistakeDifferences.size(),
                segmentPIs.size());

        return new PerformanceIndex(normalPI);
    }

    /**
     * Detects mistakes and analyzes reactions using Winsplits criteria.
     *
     * <p>Winsplits mistake criteria (both must be true):</p>
     * <ul>
     *   <li>Time loss % > median% + RELATIVE_MISTAKE_THRESHOLD_PERCENT (25%)</li>
     *   <li>Absolute time loss > ABSOLUTE_MISTAKE_THRESHOLD_SECONDS (30s)</li>
     * </ul>
     *
     * @param segmentPIs All segment PIs
     * @param normalPI The runner's normal PI
     * @param medianDifferencePercent Median time difference in percent
     * @return List of mistake-reaction pairs
     */
    private List<MistakeReactionPair> detectMistakesAndReactions(
            List<SegmentPI> segmentPIs,
            PerformanceIndex normalPI,
            double medianDifferencePercent) {

        List<MistakeReactionPair> mistakeReactions = new ArrayList<>();

        for (int i = 0; i < segmentPIs.size() - 1; i++) {
            SegmentPI currentSegment = segmentPIs.get(i);

            // Calculate time difference % for current segment
            double diffPercent = (currentSegment.pi().value() - 1.0) * 100.0;
            double timeLossSeconds = currentSegment.runnerTime() - currentSegment.bestTime();

            // Check if this is a mistake using Winsplits criteria
            boolean isMistake = diffPercent > (medianDifferencePercent + RELATIVE_MISTAKE_THRESHOLD_PERCENT)
                    && timeLossSeconds > ABSOLUTE_MISTAKE_THRESHOLD_SECONDS;

            if (isMistake) {
                SegmentPI nextSegment = segmentPIs.get(i + 1);

                // Calculate MRI
                MentalResilienceIndex mri = MentalResilienceIndex.of(nextSegment.pi, normalPI);

                // Check if reaction segment is also a mistake (chain error)
                double nextDiffPercent = (nextSegment.pi().value() - 1.0) * 100.0;
                double nextTimeLossSeconds = nextSegment.runnerTime() - nextSegment.bestTime();
                boolean nextIsMistake = nextDiffPercent > (medianDifferencePercent + RELATIVE_MISTAKE_THRESHOLD_PERCENT)
                        && nextTimeLossSeconds > ABSOLUTE_MISTAKE_THRESHOLD_SECONDS;

                MentalClassification classification;
                if (nextIsMistake) {
                    classification = MentalClassification.CHAIN_ERROR;
                    log.debug(
                        "Detected CHAIN ERROR at leg {}: mistake PI={}, reaction PI={} (also a mistake)",
                        currentSegment.legNumber,
                        String.format("%.3f", currentSegment.pi.value()),
                        String.format("%.3f", nextSegment.pi.value()));

                } else {
                    classification = mri.classify();
                    log.debug("Detected mistake at leg {}: PI={}, reaction MRI={} ({})",
                            currentSegment.legNumber,
                            String.format("%.3f", currentSegment.pi.value()),
                            String.format("%.3f", mri.value()),
                            classification);
                }

                // Create mistake-reaction pair
                MistakeReactionPair pair = new MistakeReactionPair(
                        currentSegment.legNumber,
                        new ControlCode(currentSegment.fromControl),
                        new ControlCode(currentSegment.toControl),
                        currentSegment.pi,
                        nextSegment.legNumber,
                        new ControlCode(nextSegment.fromControl),
                        new ControlCode(nextSegment.toControl),
                        nextSegment.pi,
                        mri,
                        classification
                );

                mistakeReactions.add(pair);
            }
        }

        // Check last segment for mistake (no reaction available)
        if (!segmentPIs.isEmpty()) {
            SegmentPI lastSegment = segmentPIs.getLast();
            double lastDiffPercent = (lastSegment.pi().value() - 1.0) * 100.0;
            double lastTimeLossSeconds = lastSegment.runnerTime() - lastSegment.bestTime();
            boolean lastIsMistake = lastDiffPercent > (medianDifferencePercent + RELATIVE_MISTAKE_THRESHOLD_PERCENT)
                    && lastTimeLossSeconds > ABSOLUTE_MISTAKE_THRESHOLD_SECONDS;

            if (lastIsMistake) {
                log.debug("Last segment mistake detected (diff={}%, time loss={}s) but skipped (no reaction segment available)",
                        String.format("%.1f", lastDiffPercent),
                        String.format("%.1f", lastTimeLossSeconds));
            }
        }

        return mistakeReactions;
    }

    /**
     * Calculates aggregate statistics.
     */
    private MriStatistics calculateStatistics(int totalRunners, List<RunnerMentalProfile> profiles) {
        int runnersWithMistakes = profiles.size();
        int totalMistakes = profiles.stream()
                .mapToInt(RunnerMentalProfile::getMistakeCount)
                .sum();

        int panicRunners = 0;
        int iceManRunners = 0;
        int resignerRunners = 0;

        List<Double> allMRIs = new ArrayList<>();

        for (RunnerMentalProfile profile : profiles) {
            // Count runners by their overall classification
            // Note: CHAIN_ERROR is not counted here as it's a reaction-level classification,
            // not a runner-level classification. Runners are classified based on their average MRI.
            switch (profile.classification()) {
                case PANIC -> panicRunners++;
                case ICE_MAN -> iceManRunners++;
                case RESIGNER -> resignerRunners++;
                case CHAIN_ERROR -> // This shouldn't happen at runner level, but handle gracefully
                    log.warn("Runner {} has CHAIN_ERROR as overall classification - this is unexpected",
                            profile.personId());
            }

            // Collect all individual MRIs for average/median calculation
            for (MistakeReactionPair pair : profile.mistakeReactions()) {
                allMRIs.add(pair.mri().value());
            }
        }

        Double averageMRI = allMRIs.isEmpty() ? null : allMRIs.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        Double medianMRI = calculateMedian(allMRIs);

        return new MriStatistics(
                totalRunners,
                runnersWithMistakes,
                totalMistakes,
                panicRunners,
                iceManRunners,
                resignerRunners,
                averageMRI,
                medianMRI
        );
    }

    /**
     * Calculates median value.
     */
    private @Nullable Double calculateMedian(List<Double> values) {
        if (values.isEmpty()) {
            return null;
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

    private MentalResilienceAnalysis createEmptyAnalysis(ResultListId resultListId) {
        EventId eventId = new EventId(0L); // Placeholder, will be overridden if data exists
        MriStatistics emptyStats = new MriStatistics(0, 0, 0, 0, 0, 0, null, null);
        return new MentalResilienceAnalysis(resultListId, eventId, Collections.emptyList(), emptyStats);
    }

    // Helper records for internal calculations
    private record SegmentTime(int legNumber, String fromControl, String toControl, double timeSeconds) {}

    private record SegmentPI(
            int legNumber,
            String fromControl,
            String toControl,
            double runnerTime,
            double bestTime,
            PerformanceIndex pi
    ) {}

    /**
     * Segment with time difference calculation (Winsplits approach).
     * Instead of Performance Index (ratio), we use percentage difference.
     */
    private record SegmentDifference(
            int legNumber,
            String fromControl,
            String toControl,
            double runnerTime,
            double referenceTime,
            double timeLossSeconds,      // Absolute time loss: runnerTime - referenceTime
            double timeLossPercent       // Relative time loss: (runnerTime - referenceTime) / referenceTime * 100
    ) {}
}
