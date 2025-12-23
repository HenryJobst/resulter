package de.jobst.resulter.application.analysis;

import de.jobst.resulter.application.port.SegmentPI;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.analysis.PerformanceIndex;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class SplitTimeAnalysisServiceImpl {

    public static final String FINAL_CODE = "F";
    public static final String START_CODE = "S";
    /**
     * Number of top runners to use for reference time calculation.
     * Using top 3 instead of best time alone provides robustness against outliers.
     */
    private static final int TOP_RUNNERS_FOR_REFERENCE = 3;
    /**
     * Minimum number of non-mistake segments required to calculate Normal PI.
     * A runner needs at least this many "good" segments to establish a baseline.
     */
    static final int MIN_NON_MISTAKE_SEGMENTS = 3;
    /**
     * Minimum number of runners required in a class for calculations.
     * Below this threshold, statistical validity is too low.
     */
    static final int MIN_RUNNERS_PER_CLASS_FOR_ANALYSIS = 3;
    /**
     * Relative mistake threshold in percent (Winsplits default: 25%).
     * A segment is considered a mistake if the time loss percentage exceeds
     * the normalized difference + this threshold.
     */
    static final double RELATIVE_MISTAKE_THRESHOLD_PERCENT = 25.0;
    /**
     * Absolute time loss threshold in seconds (Winsplits default: 30s).
     * A segment is only considered a mistake if BOTH the relative and absolute
     * thresholds are exceeded.
     */
    static final double ABSOLUTE_MISTAKE_THRESHOLD_SECONDS = 30.0;
    /**
     * Threshold for reliable MRI data.
     * Classes with fewer runners will get a warning indicator.
     */
    static final int RELIABLE_RUNNERS_THRESHOLD = 5;

    public SplitTimeAnalysisServiceImpl() {
    }

    /**
     * Builds a map of runtime values keyed by "personId-className-raceNumber".
     */
    Map<RuntimeKey, Double> buildRuntimeMap(ResultList resultList) {
        Map<RuntimeKey, Double> runtimeMap = new HashMap<>();

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

    private RuntimeKey buildRuntimeKey(Long personId, String className, Integer raceNumber) {
        return new RuntimeKey(personId, className, raceNumber);
    }

    /**
     * Counts the number of runners per class.
     */
    Map<String, Integer> countRunnersPerClass(List<SplitTimeList> splitTimeLists) {
        Map<String, Integer> counts = new HashMap<>();
        for (SplitTimeList splitTimeList : splitTimeLists) {
            String className = splitTimeList.getClassResultShortName().value();
            counts.put(className, counts.getOrDefault(className, 0) + 1);
        }
        return counts;
    }

    /**
     * Calculates reference time for each segment per class. Uses average of top 3 times (instead of single best time)
     * to avoid outliers. This approach is more robust and matches tools like Winsplits. Each class has its own
     * reference times to ensure fair comparison within the same age/skill group.
     */
    Map<SegmentKey, Double> calculateReferenceTimesPerSegment(
            List<SplitTimeList> splitTimeLists,
            Map<RuntimeKey, Double> runtimeMap) {

        Map<SegmentKey, List<Double>> segmentTimesMap = calculateAllTimesPerSegment(splitTimeLists, runtimeMap);

        // Calculate reference time as average of top 3 times for each segment per class
        Map<SegmentKey, Double> referenceTimes = new HashMap<>();
        for (Map.Entry<SegmentKey, List<Double>> entry : segmentTimesMap.entrySet()) {
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
                referenceTimes.put(entry.getKey(), referenceTime);
                log.trace("Segment {}: reference time = {}s (avg of top {} from {} runners)",
                        entry.getKey(),
                        String.format("%.1f", referenceTime), topN, times.size());
            }
        }

        return referenceTimes;
    }

    /**
     * Collect all times for each segment per class.
     */
    Map<SegmentKey, List<Double>> calculateAllTimesPerSegment(
        List<SplitTimeList> splitTimeLists,
        Map<RuntimeKey, Double> runtimeMap) {

        Map<SegmentKey, List<Double>> segmentTimesMap = new HashMap<>();

        for (SplitTimeList splitTimeList : splitTimeLists) {
            String className = splitTimeList.getClassResultShortName().value();
            List<SegmentTime> segmentTimes = calculateSegmentTimes(splitTimeList, runtimeMap);

            for (SegmentTime segmentTime : segmentTimes) {
                // Include className in key to calculate reference times per class
                SegmentKey segmentKey =
                    new SegmentKey(className, segmentTime.fromControl(), segmentTime.toControl());
                segmentTimesMap.computeIfAbsent(segmentKey, k -> new ArrayList<>())
                    .add(segmentTime.timeSeconds());
            }
        }
        return segmentTimesMap;
    }

    /**
     * Calculates segment times between consecutive controls.
     */
    List<SegmentTime> calculateSegmentTimes(
            SplitTimeList splitTimeList,
            Map<RuntimeKey, Double> runtimeMap) {

        List<SegmentTime> segmentTimes = new ArrayList<>();

        // Add virtual start control at 0.0 seconds
        List<SplitTime> allSplits = new ArrayList<>();
        allSplits.add(new SplitTime(ControlCode.of(START_CODE), PunchTime.of(0.0), splitTimeList.getId()));
        allSplits.addAll(splitTimeList.getSplitTimes());

        // Add virtual finish control with runtime
        RuntimeKey runtimeKey = buildRuntimeKey(
                splitTimeList.getPersonId().value(),
                splitTimeList.getClassResultShortName().value(),
                splitTimeList.getRaceNumber().value().intValue()
        );
        Double runtime = runtimeMap.get(runtimeKey);
        if (runtime != null && runtime > 0) {
            allSplits.add(new SplitTime(ControlCode.of(FINAL_CODE),
                    PunchTime.of(runtime),
                    splitTimeList.getId()));
        }

        // Sort by punch time
        allSplits.sort(Comparator.comparing(st ->
            st.punchTime().value() != null
                        ? st.punchTime().value()
                        : Double.MAX_VALUE));

        // Calculate segment times
        for (int i = 0; i < allSplits.size() - 1; i++) {
            SplitTime current = allSplits.get(i);
            SplitTime next = allSplits.get(i + 1);

            if (current.punchTime().value() != null && next.punchTime().value() != null) {
                double segmentTime = next.punchTime().value() - current.punchTime().value();
                if (segmentTime > 0) {
                    segmentTimes.add(new SegmentTime(
                            i,
                            current.controlCode().value(),
                            next.controlCode().value(),
                            segmentTime
                    ));
                }
            }
        }

        return segmentTimes;
    }

    /**
     * Calculates Performance Index for each segment. Uses class-specific reference times for fair comparison within the
     * same age/skill group.
     */
    List<SegmentPI> calculateSegmentPIs(
            List<SegmentTime> segmentTimes,
            Map<SegmentKey, Double> referenceTimesPerSegment,
            String className) {

        List<SegmentPI> segmentPIs = new ArrayList<>();

        for (SegmentTime segmentTime : segmentTimes) {
            // Include className to lookup class-specific best time
            SegmentKey segmentKey = new SegmentKey(className, segmentTime.fromControl(), segmentTime.toControl());
            Double referenceTime = referenceTimesPerSegment.get(segmentKey);

            if (referenceTime != null && referenceTime > 0) {
                PerformanceIndex pi = PerformanceIndex.of(segmentTime.timeSeconds(), referenceTime);
                segmentPIs.add(new SegmentPI(segmentTime.legNumber(),
                        segmentTime.fromControl(), segmentTime.toControl(), segmentTime.timeSeconds(),
                        referenceTime,
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
     * @param segmentPIs              All segment PIs for the runner
     * @return Normal PI (converted back from median difference %) or null if too few valid segments
     */
    @Nullable PerformanceIndex calculateNormalPI(List<SegmentPI> segmentPIs) {
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

        List<Double> nonMistakeDifferences = new ArrayList<>();
        for (SegmentPI seg : segmentPIs) {
            if (seg.toControl().equals(FINAL_CODE)) {
                // skip final segment, often much faster than normal segments (short and simple)
                continue;
            }
            MistakeResult mistakeResult = isMistakeBase(seg, medianDifferencePercent);
            if (!mistakeResult.isMistake()) {
                nonMistakeDifferences.add(mistakeResult.diffPercent());
            } else {
                log.trace("Leg {}: Mistake detected - diff={}%, time loss={}s",
                    seg.legNumber(),
                    String.format("%.1f", mistakeResult.diffPercent()),
                    String.format("%.1f", mistakeResult.timeLossSeconds()));
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
     * Calculates median value.
     *
     * @param values list of value to calculate median of
     */
    @Nullable Double calculateMedian(List<Double> values) {
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

    MistakeResult isMistakeBase(SegmentPI segmentPI, double medianDifferencePercent) {
        // Mistake using Winsplits criteria:
        // 1. Time loss % > median% + RELATIVE_MISTAKE_THRESHOLD_PERCENT (default 25%)
        // 2. Absolute time loss > ABSOLUTE_MISTAKE_THRESHOLD_SECONDS (default 30s)

        // Calculate time difference % for current segment
        double diffPercent = (segmentPI.pi().value() - 1.0) * 100.0;
        double timeLossSeconds = segmentPI.runnerTime() - segmentPI.referenceTime();

        // Check if this is a mistake using Winsplits criteria
        return new MistakeResult(diffPercent, timeLossSeconds,
        diffPercent > (medianDifferencePercent + RELATIVE_MISTAKE_THRESHOLD_PERCENT)
               && timeLossSeconds > ABSOLUTE_MISTAKE_THRESHOLD_SECONDS);
    }

    boolean isMistake(SegmentPI segmentPI, double medianDifferencePercent) {
        return isMistakeBase(segmentPI, medianDifferencePercent).isMistake();
    }
}
