package de.jobst.resulter.application.analysis;

import de.jobst.resulter.application.port.*;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.analysis.*;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.jobst.resulter.application.analysis.SplitTimeAnalysisServiceImpl.*;

/**
 * Implementation of split-time table service.
 * Generates Winsplits-style tables with cumulative/segment times,
 * positions, and individual PI-based error detection.
 */
@Service
@Slf4j
public class SplitTimeTableServiceImpl implements SplitTimeTableService {

    // Threshold for error detection (10% worse than Normal PI)
    private static final double ERROR_THRESHOLD = 0.10;

    // Minimum runners for reliable analysis
    private static final int RELIABLE_RUNNERS_THRESHOLD = 5;

    private final SplitTimeListRepository splitTimeListRepository;
    private final ResultListRepository resultListRepository;
    private final SplitTimeAnalysisServiceImpl splitTimeAnalysisService;
    private final PersonRepository personRepository;

    public SplitTimeTableServiceImpl(
            SplitTimeListRepository splitTimeListRepository,
            ResultListRepository resultListRepository,
            SplitTimeAnalysisServiceImpl splitTimeAnalysisService,
            PersonRepository personRepository) {
        this.splitTimeListRepository = splitTimeListRepository;
        this.resultListRepository = resultListRepository;
        this.splitTimeAnalysisService = splitTimeAnalysisService;
        this.personRepository = personRepository;
    }

    @Override
    public SplitTimeTable generateByClass(ResultListId resultListId, String className) {
        log.debug("Generating split-time table for result list {} by class {}", resultListId, className);

        // Fetch all split time lists
        List<SplitTimeList> allSplits = splitTimeListRepository.findByResultListId(resultListId);

        // Filter by class
        List<SplitTimeList> filteredSplits = allSplits.stream()
                .filter(stl -> stl.getClassResultShortName().value().equals(className))
                .toList();

        if (filteredSplits.isEmpty()) {
            log.warn("No split times found for class {}", className);
            return createEmptyTable("CLASS", className, List.of(className));
        }

        return generateTable(resultListId, filteredSplits, "CLASS", className, List.of(className));
    }

    @Override
    public SplitTimeTable generateByCourse(ResultListId resultListId, Long courseId) {
        log.debug("Generating split-time table for result list {} by course {}", resultListId, courseId);

        // Fetch result list to get course-class mapping
        ResultList resultList = resultListRepository.findById(resultListId)
                .orElseThrow(() -> new IllegalArgumentException("Result list not found: " + resultListId));

        // Find all classes that ran this course
        List<String> classNames = resultList.getClassResults() != null ?
                                  resultList.getClassResults()
                                      .stream()
                                      .filter(cr -> cr.courseId() != null && cr.courseId().value().equals(courseId))
                                      .map(cr -> cr.classResultShortName().value())
                                      .toList() :
                                  List.of();

        if (classNames.isEmpty()) {
            log.warn("No classes found for course {}", courseId);
            return createEmptyTable("COURSE", courseId.toString(), List.of());
        }

        // Fetch all split time lists
        List<SplitTimeList> allSplits = splitTimeListRepository.findByResultListId(resultListId);

        // Filter by classes on this course
        Set<String> classNameSet = new HashSet<>(classNames);
        List<SplitTimeList> filteredSplits = allSplits.stream()
                .filter(stl -> classNameSet.contains(stl.getClassResultShortName().value()))
                .toList();

        if (filteredSplits.isEmpty()) {
            log.warn("No split times found for course {}", courseId);
            return createEmptyTable("COURSE", courseId.toString(), classNames);
        }

        return generateTable(resultListId, filteredSplits, "COURSE", courseId.toString(), classNames);
    }

    @Override
    public List<ClassGroupOption> getAvailableClasses(ResultListId resultListId) {
        log.debug("Getting available classes for result list {}", resultListId);

        List<SplitTimeList> allSplits = splitTimeListRepository.findByResultListId(resultListId);

        // Group by class and count runners
        Map<String, Long> classCounts = allSplits.stream()
                .collect(Collectors.groupingBy(
                        stl -> stl.getClassResultShortName().value(),
                        Collectors.counting()
                ));

        return classCounts.entrySet().stream()
                .map(entry -> new ClassGroupOption(entry.getKey(), entry.getValue().intValue()))
                .sorted(Comparator.comparing(ClassGroupOption::className))
                .toList();
    }

    @Override
    public List<CourseGroupOption> getAvailableCourses(ResultListId resultListId) {
        log.debug("Getting available courses for result list {}", resultListId);

        ResultList resultList = resultListRepository.findById(resultListId)
                .orElseThrow(() -> new IllegalArgumentException("Result list not found: " + resultListId));

        List<SplitTimeList> allSplits = splitTimeListRepository.findByResultListId(resultListId);

        // Group classes by course
        Map<Long, List<ClassResult>> courseToClasses = resultList.getClassResults() != null ?
                                                       resultList.getClassResults()
                                                           .stream()
                                                           .filter(cr -> cr.courseId() != null)
                                                           .collect(Collectors.groupingBy(cr -> cr.courseId()
                                                               .value())) :
                                                       Map.of();

        // Count runners per class
        Map<String, Long> classRunnerCounts = allSplits.stream()
                .collect(Collectors.groupingBy(
                        stl -> stl.getClassResultShortName().value(),
                        Collectors.counting()
                ));

        // Build course options
        List<CourseGroupOption> options = new ArrayList<>();
        for (Map.Entry<Long, List<ClassResult>> entry : courseToClasses.entrySet()) {
            Long courseId = entry.getKey();
            List<ClassResult> classes = entry.getValue();

            List<String> classNames = classes.stream()
                    .map(cr -> cr.classResultShortName().value())
                    .toList();

            int totalRunners = classNames.stream()
                    .mapToInt(className -> classRunnerCounts.getOrDefault(className, 0L).intValue())
                    .sum();

            // Get course name from first class
            String courseName = classes.getFirst().courseId() != null
                    ? "Course " + courseId  // Simplified - would need CourseRepository to get real name
                    : "Unknown";

            options.add(new CourseGroupOption(courseId, courseName, classNames, totalRunners));
        }

        return options.stream()
                .sorted(Comparator.comparing(CourseGroupOption::courseName))
                .toList();
    }

    /**
     * Main table generation logic.
     */
    private SplitTimeTable generateTable(
            ResultListId resultListId,
            List<SplitTimeList> filteredSplits,
            String groupByType,
            String groupId,
            List<String> groupNames) {

        log.debug("Generating table for {} runners", filteredSplits.size());

        // Fetch result list for runtime data
        ResultList resultList = resultListRepository.findById(resultListId)
                .orElseThrow(() -> new IllegalArgumentException("Result list not found: " + resultListId));

        // Build runtime map
        Map<RuntimeKey, Double> runtimeMap = splitTimeAnalysisService.buildRuntimeMap(resultList);

        // Build map of PersonId -> ResultStatus
        Map<PersonId, ResultStatus> notCompetingMap = buildNotCompetingMap(resultList);

        // Extract control codes
        List<String> controlCodes = extractControlCodes(filteredSplits, runtimeMap);

        // Calculate cumulative times aligned to control sequence (position-indexed)
        Map<Long, List<Double>> cumulativeTimes = calculateCumulativeTimes(filteredSplits, controlCodes, runtimeMap);

        // Calculate cumulative positions (position-indexed)
        Map<Integer, Map<Long, Integer>> cumulativePositions = calculatePositions(cumulativeTimes, controlCodes);

        // Calculate segment times and positions (position-indexed)
        Map<SegmentIndexPersonKey, Double> segmentTimes = calculateSegmentTimes(cumulativeTimes, controlCodes);
        Map<SegmentIndexKey, Map<Long, Integer>> segmentPositions = calculateSegmentPositions(segmentTimes, controlCodes);

        // Detect errors using individual PI
        Map<RunnerSegmentKey, ErrorInfo> errorMap = detectErrors(filteredSplits, runtimeMap);

        // Identify best times (position-indexed)
        Map<Integer, Double> bestCumulativeTimes = findBestTimes(cumulativeTimes, controlCodes, notCompetingMap);
        Map<SegmentIndexKey, Double> bestSegmentTimes = findBestSegmentTimes(segmentTimes);

        // Assemble rows
        List<SplitTimeTableRow> rows = assembleRows(
                filteredSplits,
                controlCodes,
                cumulativeTimes,
                cumulativePositions,
                segmentTimes,
                segmentPositions,
                errorMap,
                bestCumulativeTimes,
                bestSegmentTimes,
                notCompetingMap
        );

        // Assign positions to competing runners only
        rows = assignPositions(rows);

        // Calculate winner time (minimum finish time among competing runners)
        Double winnerTime = rows.stream()
                .filter(row -> !row.notCompeting())
                .map(SplitTimeTableRow::finishTime)
                .filter(Objects::nonNull)
                .min(Double::compare)
                .orElse(null);

        // Calculate metadata
        int runnersWithCompleteSplits = (int) filteredSplits.stream()
                .filter(stl -> hasCompleteSplits(stl, controlCodes))
                .count();

        SplitTimeTableMetadata metadata = new SplitTimeTableMetadata(
                filteredSplits.size(),
                runnersWithCompleteSplits,
                controlCodes.size(),
                filteredSplits.size() >= RELIABLE_RUNNERS_THRESHOLD,
                winnerTime
        );

        return new SplitTimeTable(groupByType, groupId, groupNames, controlCodes, rows, metadata);
    }

    /**
     * Extract control codes in punch time order from a runner with the most splits.
     * The punch time order represents the correct course control sequence.
     */
    private List<String> extractControlCodes(List<SplitTimeList> splits, Map<RuntimeKey, Double> runtimeMap) {
        // Find a runner with the most splits to get the correct control order
        SplitTimeList referenceSplit = splits.stream()
                .max(Comparator.comparingInt(stl -> stl.getSplitTimes().size()))
                .orElse(null);

        if (referenceSplit == null || referenceSplit.getSplitTimes().isEmpty()) {
            // Fallback: return just START and FINISH
            return new ArrayList<>(List.of(START_CODE, FINAL_CODE));
        }

        // Get controls in punch time order (SplitTime is Comparable by punchTime)
        List<String> controlCodes = new ArrayList<>();
        controlCodes.add(START_CODE); // Virtual start

        referenceSplit.getSplitTimes().stream().sorted() // Sort by punch time (natural order)
            .map(split -> split.controlCode().value())
                .forEach(controlCodes::add);

        // Add virtual finish if any runner has runtime
        boolean hasRuntimes = splits.stream()
                .anyMatch(stl -> {
                    RuntimeKey key = new RuntimeKey(
                            stl.getPersonId().value(),
                            stl.getClassResultShortName().value(),
                            stl.getRaceNumber().value().intValue()
                    );
                    return runtimeMap.containsKey(key);
                });

        if (hasRuntimes) {
            controlCodes.add(FINAL_CODE); // Virtual finish
        }

        return controlCodes;
    }

    /**
     * Calculate cumulative times for all runners at all controls.
     * Returns a map of personId -> list of cumulative times (indexed by position in controlCodes).
     */
    private Map<Long, List<Double>> calculateCumulativeTimes(
            List<SplitTimeList> splits,
            List<String> controlCodes,
            Map<RuntimeKey, Double> runtimeMap) {

        Map<Long, List<Double>> cumulativeTimes = new HashMap<>();

        for (SplitTimeList stl : splits) {
            Long personId = stl.getPersonId().value();
            List<Double> cumTimes = buildAlignedCumulativeTimes(stl, controlCodes, runtimeMap);

            cumulativeTimes.put(personId, cumTimes);
        }

        return cumulativeTimes;
    }

    /**
     * Build a fixed-size cumulative list aligned to the reference control sequence.
     * Missing controls stay null so subsequent punches are not shifted to wrong controls.
     */
    private List<Double> buildAlignedCumulativeTimes(
            SplitTimeList splitTimeList,
            List<String> controlCodes,
            Map<RuntimeKey, Double> runtimeMap) {

        List<Double> aligned = new ArrayList<>(Collections.nCopies(controlCodes.size(), null));

        int startIndex = controlCodes.indexOf(START_CODE);
        if (startIndex >= 0) {
            aligned.set(startIndex, 0.0);
        }

        List<SplitTime> runnerSplits = splitTimeList.getSplitTimes().stream()
                .filter(split -> split.controlCode() != null && split.controlCode().value() != null)
                .filter(split -> split.punchTime().value() != null)
                .filter(split -> split.punchTime().value() > 0.0)
                .sorted()
                .toList();

        List<Integer> referenceControlIndices = new ArrayList<>();
        for (int i = 0; i < controlCodes.size(); i++) {
            String code = controlCodes.get(i);
            if (!code.equals(START_CODE) && !code.equals(FINAL_CODE)) {
                referenceControlIndices.add(i);
            }
        }

        List<String> referenceCodes = referenceControlIndices.stream()
                .map(controlCodes::get)
                .toList();
        List<String> runnerCodes = runnerSplits.stream()
                .map(split -> split.controlCode().value())
                .toList();

        List<MatchIndex> matches = longestCommonSubsequenceMatches(referenceCodes, runnerCodes);

        for (MatchIndex match : matches) {
            int controlIndex = referenceControlIndices.get(match.referenceIndex());
            Double punchTime = runnerSplits.get(match.runnerIndex()).punchTime().value();
            aligned.set(controlIndex, punchTime);
        }

        int comparable = Math.min(referenceCodes.size(), runnerSplits.size());
        boolean tooFewMatches = comparable > 0 && matches.size() * 2 < comparable;
        if (tooFewMatches) {
            for (int i = 0; i < comparable; i++) {
                int controlIndex = referenceControlIndices.get(i);
                aligned.set(controlIndex, runnerSplits.get(i).punchTime().value());
            }
        }

        int finishIndex = controlCodes.indexOf(FINAL_CODE);
        if (finishIndex >= 0) {
            RuntimeKey runtimeKey = new RuntimeKey(
                    splitTimeList.getPersonId().value(),
                    splitTimeList.getClassResultShortName().value(),
                    splitTimeList.getRaceNumber().value().intValue()
            );
            Double runtime = runtimeMap.get(runtimeKey);
            if (runtime != null && runtime > 0) {
                aligned.set(finishIndex, runtime);
            }
        }

        return aligned;
    }

    private List<MatchIndex> longestCommonSubsequenceMatches(List<String> referenceCodes, List<String> runnerCodes) {
        int n = referenceCodes.size();
        int m = runnerCodes.size();

        if (n == 0 || m == 0) {
            return List.of();
        }

        int[][] dp = new int[n + 1][m + 1];
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                if (Objects.equals(referenceCodes.get(i), runnerCodes.get(j))) {
                    dp[i][j] = 1 + dp[i + 1][j + 1];
                } else {
                    dp[i][j] = Math.max(dp[i + 1][j], dp[i][j + 1]);
                }
            }
        }

        List<MatchIndex> matches = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < n && j < m) {
            if (Objects.equals(referenceCodes.get(i), runnerCodes.get(j))) {
                matches.add(new MatchIndex(i, j));
                i++;
                j++;
            } else if (dp[i + 1][j] >= dp[i][j + 1]) {
                i++;
            } else {
                j++;
            }
        }

        return matches;
    }

    /**
     * Calculate positions at each control based on cumulative times.
     * Uses position/index in controlCodes list instead of control code (to handle butterfly loops).
     */
    private Map<Integer, Map<Long, Integer>> calculatePositions(
            Map<Long, List<Double>> cumulativeTimes,
            List<String> controlCodes) {

        Map<Integer, Map<Long, Integer>> positions = new HashMap<>();

        for (int controlIndex = 0; controlIndex < controlCodes.size(); controlIndex++) {
            final int idx = controlIndex; // For lambda capture

            // Get all times at this control position
            List<Map.Entry<Long, Double>> timesAtControl = cumulativeTimes.entrySet().stream()
                    .filter(entry -> entry.getValue().size() > idx && entry.getValue().get(idx) != null)
                    .map(entry -> Map.entry(entry.getKey(), entry.getValue().get(idx)))
                    .sorted(Map.Entry.comparingByValue())
                    .toList();

            // Assign positions
            Map<Long, Integer> controlPositions = new HashMap<>();
            for (int i = 0; i < timesAtControl.size(); i++) {
                controlPositions.put(timesAtControl.get(i).getKey(), i + 1);
            }

            positions.put(controlIndex, controlPositions);
        }

        return positions;
    }

    /**
     * Calculate segment times from cumulative times.
     * Uses position/index in controlCodes list instead of control code (to handle butterfly loops).
     */
    private Map<SegmentIndexPersonKey, Double> calculateSegmentTimes(
            Map<Long, List<Double>> cumulativeTimes,
            List<String> controlCodes) {

        Map<SegmentIndexPersonKey, Double> segmentTimes = new HashMap<>();

        // Calculate segment times for each person
        for (Map.Entry<Long, List<Double>> personEntry : cumulativeTimes.entrySet()) {
            Long personId = personEntry.getKey();
            List<Double> times = personEntry.getValue();

            // Calculate segment time for each consecutive pair of controls
            IntStream.range(1, Math.min(times.size(), controlCodes.size()))
                .forEachOrdered(i -> {
                    Double fromTime = times.get(i - 1);
                    Double toTime = times.get(i);
                    if (fromTime != null && toTime != null) {
                        double segmentTime = toTime - fromTime;
                        SegmentIndexPersonKey key = new SegmentIndexPersonKey(i - 1, i, personId);
                        segmentTimes.put(key, segmentTime);
                    }
                });
        }

        return segmentTimes;
    }

    /**
     * Calculate positions for each segment.
     * Uses position/index instead of control codes (to handle butterfly loops).
     */
    private Map<SegmentIndexKey, Map<Long, Integer>> calculateSegmentPositions(
            Map<SegmentIndexPersonKey, Double> segmentTimes,
            List<String> controlCodes) {

        Map<SegmentIndexKey, Map<Long, Integer>> positions = new HashMap<>();

        for (int i = 1; i < controlCodes.size(); i++) {
            final int fromIdx = i - 1;
            final int toIdx = i;
            SegmentIndexKey segmentKey = new SegmentIndexKey(fromIdx, toIdx);

            // Get all times for this segment
            List<Map.Entry<Long, Double>> timesForSegment = segmentTimes.entrySet().stream()
                    .filter(entry -> entry.getKey().fromIndex() == fromIdx
                            && entry.getKey().toIndex() == toIdx)
                    .map(entry -> Map.entry(entry.getKey().personId(), entry.getValue()))
                    .sorted(Map.Entry.comparingByValue())
                    .toList();

            // Assign positions
            Map<Long, Integer> segmentPositions = IntStream.range(0, timesForSegment.size())
                .boxed()
                .collect(Collectors.toMap(j -> timesForSegment.get(j).getKey(), j -> j + 1, (a, b) -> b));

            positions.put(segmentKey, segmentPositions);
        }

        return positions;
    }

    /**
     * Detect errors using individual PI-based approach.
     * Error if segment-PI > Normal-PI + threshold.
     */
    private Map<RunnerSegmentKey, ErrorInfo> detectErrors(
            List<SplitTimeList> splits,
            Map<RuntimeKey, Double> runtimeMap) {

        Map<RunnerSegmentKey, ErrorInfo> errorMap = new HashMap<>();

        // Calculate reference times per segment
        Map<SegmentKey, Double> referenceTimes =
                splitTimeAnalysisService.calculateReferenceTimesPerSegment(splits, runtimeMap);

        for (SplitTimeList stl : splits) {
            Long personId = stl.getPersonId().value();
            String className = stl.getClassResultShortName().value();

            // Calculate segment times and PIs
            List<SegmentTime> segmentTimes = splitTimeAnalysisService.calculateSegmentTimes(stl, runtimeMap);
            List<SegmentPI> segmentPIs = splitTimeAnalysisService.calculateSegmentPIs(
                    segmentTimes, referenceTimes, className);

            // Calculate Normal PI for this runner
            PerformanceIndex normalPI = splitTimeAnalysisService.calculateNormalPI(segmentPIs);

            if (normalPI != null) {
                // Check each segment for errors
                for (SegmentPI segmentPI : segmentPIs) {
                    // Skip start and final segments
                    if (segmentPI.fromControl().equals(START_CODE) || segmentPI.toControl().equals(FINAL_CODE)) {
                        continue;
                    }

                    double segmentPIValue = segmentPI.pi().value();
                    double normalPIValue = normalPI.value();
                    double magnitude = segmentPIValue - normalPIValue;

                    if (magnitude > ERROR_THRESHOLD) {
                        ErrorSeverity severity = classifyErrorSeverity(magnitude);
                        RunnerSegmentKey key = new RunnerSegmentKey(
                                personId,
                                segmentPI.fromControl(),
                                segmentPI.toControl()
                        );
                        errorMap.put(key, new ErrorInfo(true, severity, magnitude));
                    }
                }
            }
        }

        return errorMap;
    }

    /**
     * Classify error severity based on magnitude.
     */
    private ErrorSeverity classifyErrorSeverity(double magnitude) {
        if (magnitude >= 0.50) return ErrorSeverity.SEVERE;  // 50%+ worse
        if (magnitude >= 0.30) return ErrorSeverity.HIGH;    // 30-50% worse
        if (magnitude >= 0.15) return ErrorSeverity.MEDIUM;  // 15-30% worse
        if (magnitude >= 0.05) return ErrorSeverity.LOW;     // 5-15% worse
        return ErrorSeverity.NONE;
    }

    /**
     * Find the best cumulative times at each control position.
     * Uses position/index instead of control codes (to handle butterfly loops).
     * Excludes runners marked as NOT_COMPETING (AK).
     */
    private Map<Integer, Double> findBestTimes(
            Map<Long, List<Double>> cumulativeTimes,
            List<String> controlCodes,
            Map<PersonId, ResultStatus> notCompetingMap) {

        Map<Integer, Double> bestTimes = new HashMap<>();

        for (int controlIndex = 0; controlIndex < controlCodes.size(); controlIndex++) {
            final int idx = controlIndex; // For lambda capture

            // Filter out AK runners when finding the best cumulative times
            Double best = cumulativeTimes.entrySet().stream()
                    .filter(entry -> {
                        PersonId personId = new PersonId(entry.getKey());
                        ResultStatus status = notCompetingMap.get(personId);
                        return status != ResultStatus.NOT_COMPETING;
                    })
                    .map(Map.Entry::getValue)
                    .filter(times -> times.size() > idx && times.get(idx) != null)
                    .map(times -> times.get(idx))
                    .min(Double::compare)
                    .orElse(null);

            if (best != null) {
                bestTimes.put(controlIndex, best);
            }
        }

        return bestTimes;
    }

    /**
     * Find best segment times for each segment.
     * Uses position/index instead of control codes (to handle butterfly loops).
     */
    private Map<SegmentIndexKey, Double> findBestSegmentTimes(Map<SegmentIndexPersonKey, Double> segmentTimes) {
        Map<SegmentIndexKey, Double> bestTimes = new HashMap<>();

        // Group by segment
        Map<SegmentIndexKey, List<Double>> timesBySegment = new HashMap<>();
        segmentTimes.forEach((key1, value) -> {
            SegmentIndexKey key = new SegmentIndexKey(key1.fromIndex(), key1.toIndex());
            timesBySegment.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        });

        // Find best for each segment
        timesBySegment.forEach((key, value) -> value.stream()
            .min(Double::compare)
            .ifPresent(best -> bestTimes.put(key, best)));

        return bestTimes;
    }

    /**
     * Assemble table rows from calculated data.
     * Uses position/index-based lookups to handle butterfly loops correctly.
     */
    private List<SplitTimeTableRow> assembleRows(
            List<SplitTimeList> splits,
            List<String> controlCodes,
            Map<Long, List<Double>> cumulativeTimes,
            Map<Integer, Map<Long, Integer>> cumulativePositions,
            Map<SegmentIndexPersonKey, Double> segmentTimes,
            Map<SegmentIndexKey, Map<Long, Integer>> segmentPositions,
            Map<RunnerSegmentKey, ErrorInfo> errorMap,
            Map<Integer, Double> bestCumulativeTimes,
            Map<SegmentIndexKey, Double> bestSegmentTimes,
            Map<PersonId, ResultStatus> notCompetingMap) {

        List<SplitTimeTableRow> rows = new ArrayList<>();

        // Fetch all persons at once for efficiency
        Set<PersonId> personIds = splits.stream()
                .map(SplitTimeList::getPersonId)
                .collect(Collectors.toSet());

        Map<PersonId, Person> personMap = personRepository.findAllById(personIds);

        for (SplitTimeList stl : splits) {
            PersonId personId = stl.getPersonId();
            Long personIdValue = personId.value();

            // Get actual person name or fallback to ID
            String personName = personMap.containsKey(personId)
                    ? personMap.get(personId).personName().getFullName()
                    : "Person " + personIdValue;

            String className = stl.getClassResultShortName().value();

            // Check if runner is NOT_COMPETING (AK)
            boolean notCompeting = notCompetingMap.getOrDefault(personId, ResultStatus.OK) == ResultStatus.NOT_COMPETING;

            List<SplitTimeTableCell> cells = new ArrayList<>();

            // Get this person's cumulative times
            List<Double> personCumTimes = cumulativeTimes.getOrDefault(personIdValue, List.of());

            // Extract finish time (cumulative time at finish control)
            int finishIndex = controlCodes.indexOf(FINAL_CODE);
            Double finishTime = finishIndex >= 0 && finishIndex < personCumTimes.size()
                    ? personCumTimes.get(finishIndex)
                    : null;

            for (int i = 0; i < controlCodes.size(); i++) {
                String controlCode = controlCodes.get(i);

                // Get cumulative data using index
                Double cumTime = i < personCumTimes.size() ? personCumTimes.get(i) : null;
                // Don't show positions for AK runners
                Integer cumPos = notCompeting ? null : cumulativePositions.getOrDefault(i, Map.of()).get(personIdValue);

                // Get segment data (skip only for START control itself)
                Double segTime = null;
                Integer segPos = null;
                boolean isError = false;
                ErrorSeverity severity = ErrorSeverity.NONE;
                Double errorMagnitude = null;

                // Calculate segment data for all controls except START
                if (!controlCode.equals(START_CODE) && i > 0) {
                    String fromControl = controlCodes.get(i - 1);

                    // Use index-based lookup for segment time
                    SegmentIndexPersonKey segmentPersonKey = new SegmentIndexPersonKey(i - 1, i, personIdValue);
                    segTime = segmentTimes.get(segmentPersonKey);

                    // Show segment positions for all runners (including AK)
                    SegmentIndexKey segmentKey = new SegmentIndexKey(i - 1, i);
                    segPos = segmentPositions.getOrDefault(segmentKey, Map.of()).get(personIdValue);

                    // If intermediate controls are missing, derive a per-runner segment from the
                    // last available cumulative time to the current control.
                    if (segTime == null && cumTime != null) {
                        int previousKnownIndex = findPreviousKnownIndex(personCumTimes, i - 1);
                        if (previousKnownIndex >= 0) {
                            Double previousTime = personCumTimes.get(previousKnownIndex);
                            if (previousTime != null) {
                                segTime = cumTime - previousTime;
                                segPos = null; // Not comparable across runners (different origin control)
                                fromControl = controlCodes.get(previousKnownIndex);
                            }
                        }
                    }

                    // Check for error (still uses control codes for error detection)
                    RunnerSegmentKey errorKey = new RunnerSegmentKey(personIdValue, fromControl, controlCode);
                    ErrorInfo errorInfo = errorMap.get(errorKey);
                    if (errorInfo != null) {
                        isError = errorInfo.isError();
                        severity = errorInfo.severity();
                        errorMagnitude = errorInfo.magnitude();
                    }
                }

                // Check if best times using index-based lookup
                boolean isBestCum = cumTime != null && cumTime.equals(bestCumulativeTimes.get(i));
                boolean isBestSeg = false;
                if (i > 0 && segTime != null) {
                    SegmentIndexKey segmentKey = new SegmentIndexKey(i - 1, i);
                    isBestSeg = segTime.equals(bestSegmentTimes.get(segmentKey));
                }

                cells.add(new SplitTimeTableCell(
                        controlCode,
                        cumTime,
                        cumPos,
                        segTime,
                        segPos,
                        isError,
                        severity,
                        errorMagnitude,
                        isBestCum,
                        isBestSeg
                ));
            }

            boolean hasIncompleteSplits = hasMissingIntermediateCumulative(cells);

            // Position will be assigned later after all rows are created
            rows.add(new SplitTimeTableRow(personIdValue, personName, className, cells, hasIncompleteSplits, notCompeting, finishTime, null));
        }

        return rows;
    }

    /**
     * Check if runner has complete splits for all controls.
     */
    private boolean hasCompleteSplits(SplitTimeList stl, List<String> controlCodes) {
        Map<String, Long> requiredCounts = controlCodes.stream()
                .filter(controlCode -> !controlCode.equals(START_CODE) && !controlCode.equals(FINAL_CODE))
                .collect(Collectors.groupingBy(code -> code, Collectors.counting()));

        Map<String, Long> runnerCounts = stl.getSplitTimes().stream()
                .map(split -> split.controlCode().value())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(code -> code, Collectors.counting()));

        return requiredCounts.entrySet().stream()
                .allMatch(entry -> runnerCounts.getOrDefault(entry.getKey(), 0L) >= entry.getValue());
    }

    /**
     * Build a map of PersonId -> ResultStatus for identifying NOT_COMPETING runners.
     */
    private Map<PersonId, ResultStatus> buildNotCompetingMap(ResultList resultList) {
        Map<PersonId, ResultStatus> statusMap = new HashMap<>();

        if (resultList.getClassResults() != null) {
            resultList.getClassResults().forEach(classResult -> classResult.personResults().value().forEach(personResult -> personResult.personRaceResults().value().forEach(personRaceResult -> statusMap.put(personResult.personId(), personRaceResult.getState()))));
        }

        return statusMap;
    }

    /**
     * Create empty table for error cases.
     */
    private SplitTimeTable createEmptyTable(String groupByType, String groupId, List<String> groupNames) {
        SplitTimeTableMetadata metadata = new SplitTimeTableMetadata(0, 0, 0, false, null);
        return new SplitTimeTable(groupByType, groupId, groupNames, List.of(), List.of(), metadata);
    }

    // Helper records

    /**
     * Assign overall positions to competing runners based on finish time.
     * AK (NOT_COMPETING) runners receive null position.
     */
    private List<SplitTimeTableRow> assignPositions(List<SplitTimeTableRow> rows) {
        // Sort competing runners by finish time
        List<SplitTimeTableRow> competingRunners = rows.stream()
                .filter(row -> !row.notCompeting())
                .filter(row -> !row.hasIncompleteSplits())
                .filter(row -> row.finishTime() != null)
                .sorted(Comparator.comparing(SplitTimeTableRow::finishTime))
                .toList();

        // Assign positions to competing runners
        Map<Long, Integer> positionMap = new HashMap<>();
        for (int i = 0; i < competingRunners.size(); i++) {
            positionMap.put(competingRunners.get(i).personId(), i + 1);
        }

        // Create new rows with positions assigned
        List<SplitTimeTableRow> rowsWithPositions = rows.stream()
                .map(row -> {
                    Integer position = (row.notCompeting() || row.hasIncompleteSplits())
                            ? null
                            : positionMap.get(row.personId());
                    return new SplitTimeTableRow(
                            row.personId(),
                            row.personName(),
                            row.className(),
                            row.cells(),
                            row.hasIncompleteSplits(),
                            row.notCompeting(),
                            row.finishTime(),
                            position
                    );
                })
                .toList();

        // Stable backend ordering:
        // 1) Competing runners with complete splits (by overall position)
        // 2) Competing runners with incomplete splits (by finish time if present)
        // 3) Not competing runners (AK) (by finish time if present)
        return rowsWithPositions.stream()
                .sorted(Comparator
                        .comparingInt(this::rowGroupPriority)
                        .thenComparing(row -> row.position() != null ? row.position() : Integer.MAX_VALUE)
                        .thenComparing(row -> row.finishTime() != null ? row.finishTime() : Double.MAX_VALUE))
                .toList();
    }

    private int rowGroupPriority(SplitTimeTableRow row) {
        if (row.notCompeting()) {
            return 2;
        }
        if (row.hasIncompleteSplits()) {
            return 1;
        }
        return 0;
    }

    private int findPreviousKnownIndex(List<Double> cumulativeTimes, int startIndex) {
        for (int i = startIndex; i >= 0; i--) {
            if (i < cumulativeTimes.size() && cumulativeTimes.get(i) != null) {
                return i;
            }
        }
        return -1;
    }

    /**
     * A runner is incomplete if any intermediate control (excluding virtual start/finish)
     * has no cumulative time in the generated row.
     */
    private boolean hasMissingIntermediateCumulative(List<SplitTimeTableCell> cells) {
        for (SplitTimeTableCell cell : cells) {
            String code = cell.controlCode();
            if (!START_CODE.equals(code) && !FINAL_CODE.equals(code) && cell.cumulativeTime() == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Key for segment times using position indices instead of control codes.
     * This allows proper handling of butterfly loops where controls are visited multiple times.
     */
    private record SegmentIndexPersonKey(int fromIndex, int toIndex, Long personId) {}

    /**
     * Key for segment without person (for grouping by segment position).
     */
    private record SegmentIndexKey(int fromIndex, int toIndex) {}

    private record MatchIndex(int referenceIndex, int runnerIndex) {}

    /**
     * Key for error detection (still uses control codes since errors are detected via PI analysis).
     */
    private record RunnerSegmentKey(Long personId, String fromControl, String toControl) {}

    /**
     * Error information for a segment.
     */
    private record ErrorInfo(boolean isError, ErrorSeverity severity, double magnitude) {}
}
