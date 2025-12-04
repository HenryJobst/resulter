package de.jobst.resulter.application;

import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.analysis.ControlSegment;
import de.jobst.resulter.domain.analysis.RunnerSplit;
import de.jobst.resulter.domain.analysis.SplitTimeAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SplitTimeAnalysisServiceImpl implements SplitTimeAnalysisService {

    // Maximum number of runners to include per segment (to limit response size)
    private static final int MAX_RUNNERS_PER_SEGMENT = 100;

    private final SplitTimeListRepository splitTimeListRepository;
    private final PersonRepository personRepository;
    private final ResultListRepository resultListRepository;

    public SplitTimeAnalysisServiceImpl(
            SplitTimeListRepository splitTimeListRepository,
            PersonRepository personRepository,
            ResultListRepository resultListRepository) {
        this.splitTimeListRepository = splitTimeListRepository;
        this.personRepository = personRepository;
        this.resultListRepository = resultListRepository;
    }

    @Override
    public List<SplitTimeAnalysis> analyzeSplitTimesRanking(
            ResultListId resultListId,
            boolean mergeBidirectional,
            List<String> filterNames) {

        // Fetch all split time lists for the result list
        List<SplitTimeList> splitTimeLists = splitTimeListRepository.findByResultListId(resultListId);

        if (splitTimeLists.isEmpty()) {
            log.debug("No split time data found for result list {}", resultListId);
            return List.of();
        }

        // Get person IDs and fetch person names
        Set<PersonId> personIds = splitTimeLists.stream()
                .map(SplitTimeList::getPersonId)
                .collect(Collectors.toSet());

        Map<PersonId, Person> personMap = personRepository.findAllById(personIds);

        // Fetch result list to get finish times (runtime) for each person
        ResultList resultList = resultListRepository.findById(resultListId)
                .orElseThrow(() -> new IllegalArgumentException("ResultList not found: " + resultListId));

        // Build map of (PersonId, ClassResultShortName, RaceNumber) -> runtime
        Map<String, Double> runtimeMap = buildRuntimeMap(resultList);

        // Get eventId (same for all split time lists)
        EventId eventId = splitTimeLists.getFirst().getEventId();

        // Calculate control segments across all classes
        // (Different classes may use the same course or share segments)
        List<ControlSegment> controlSegments = calculateControlSegments(
                splitTimeLists,
                personMap,
                runtimeMap,
                mergeBidirectional,
                filterNames
        );

        // Create a single analysis with all classes combined
        SplitTimeAnalysis analysis = new SplitTimeAnalysis(
                resultListId,
                eventId,
                ClassResultShortName.of("Alle Klassen"),
                controlSegments
        );

        return List.of(analysis);
    }

    private List<ControlSegment> calculateControlSegments(
            List<SplitTimeList> splitTimeLists,
            Map<PersonId, Person> personMap,
            Map<String, Double> runtimeMap,
            boolean mergeBidirectional,
            List<String> filterNames) {

        // Build a map of control sequences
        Map<String, Map<String, List<RunnerSplitData>>> segmentMap = new HashMap<>();

        for (SplitTimeList splitTimeList : splitTimeLists) {
            List<SplitTime> splitTimes = splitTimeList.getSplitTimes();

            // Add virtual Start and Finish controls
            List<SplitTime> extendedSplitTimes = addStartAndFinishControls(
                    splitTimes,
                    splitTimeList.getPersonId(),
                    splitTimeList.getClassResultShortName(),
                    splitTimeList.getRaceNumber(),
                    runtimeMap
            );

            // Sort split times by punch time to ensure chronological order
            // Use nullsLast to handle missing punches (DNF/missed controls)
            List<SplitTime> sortedSplitTimes = new ArrayList<>(extendedSplitTimes);
            sortedSplitTimes.sort(Comparator.comparing(
                    st -> st.getPunchTime().value(),
                    Comparator.nullsLast(Comparator.naturalOrder())
            ));

            // Calculate split times between consecutive controls
            for (int i = 1; i < sortedSplitTimes.size(); i++) {
                SplitTime previousSplit = sortedSplitTimes.get(i - 1);
                SplitTime currentSplit = sortedSplitTimes.get(i);

                // Skip if either punch time is null (missed control/DNF)
                if (previousSplit.getPunchTime().value() == null || currentSplit.getPunchTime().value() == null) {
                    continue;
                }

                String fromControl = previousSplit.getControlCode().value();
                String toControl = currentSplit.getControlCode().value();

                // Calculate split time (current - previous)
                Double splitTimeSeconds = currentSplit.getPunchTime().value() - previousSplit.getPunchTime().value();

                // Get person name
                Person person = personMap.get(splitTimeList.getPersonId());
                String personName = person != null ?
                        person.getPersonName().familyName().value() + " " + person.getPersonName().givenName().value() :
                        "Unknown";

                // Apply name filtering if specified
                if (!filterNames.isEmpty()) {
                    boolean matchesFilter = filterNames.stream()
                            .anyMatch(filterName ->
                                    personName.toLowerCase().contains(filterName.toLowerCase()));
                    if (!matchesFilter) {
                        continue; // Skip this runner
                    }
                }

                // Create runner split data
                RunnerSplitData runnerSplitData = new RunnerSplitData(
                        splitTimeList.getPersonId(),
                        personName,
                        splitTimeList.getClassResultShortName().value(),
                        splitTimeSeconds
                );

                // Add to segment map
                segmentMap
                        .computeIfAbsent(fromControl, k -> new HashMap<>())
                        .computeIfAbsent(toControl, k -> new ArrayList<>())
                        .add(runnerSplitData);
            }
        }

        // Convert segment map to ControlSegment objects
        List<ControlSegment> controlSegments = new ArrayList<>();

        for (Map.Entry<String, Map<String, List<RunnerSplitData>>> fromEntry : segmentMap.entrySet()) {
            String fromControl = fromEntry.getKey();

            for (Map.Entry<String, List<RunnerSplitData>> toEntry : fromEntry.getValue().entrySet()) {
                String toControl = toEntry.getKey();
                List<RunnerSplitData> runnerData = toEntry.getValue();

                // Sort by split time
                runnerData.sort(Comparator.comparingDouble(RunnerSplitData::splitTimeSeconds));

                // Limit to top N runners per segment to keep response size manageable
                int totalRunners = runnerData.size();
                int limitedSize = Math.min(totalRunners, MAX_RUNNERS_PER_SEGMENT);
                if (totalRunners > MAX_RUNNERS_PER_SEGMENT) {
                    log.debug("Limiting segment {}→{} from {} to {} runners",
                            fromControl, toControl, totalRunners, MAX_RUNNERS_PER_SEGMENT);
                }

                // Collect unique classes that use this segment
                List<String> classes = runnerData.stream()
                        .map(RunnerSplitData::classResultShortName)
                        .distinct()
                        .sorted()
                        .toList();

                // Calculate positions and time behind leader
                List<RunnerSplit> runnerSplits = new ArrayList<>();
                Double leaderTime = runnerData.isEmpty() ? 0.0 : runnerData.getFirst().splitTimeSeconds();

                for (int i = 0; i < limitedSize; i++) {
                    RunnerSplitData data = runnerData.get(i);
                    int position = i + 1;
                    Double timeBehind = data.splitTimeSeconds() - leaderTime;

                    runnerSplits.add(new RunnerSplit(
                            data.personId(),
                            data.personName(),
                            data.classResultShortName(),
                            position,
                            data.splitTimeSeconds(),
                            timeBehind
                    ));
                }

                ControlSegment segment = new ControlSegment(
                        ControlCode.of(fromControl),
                        ControlCode.of(toControl),
                        runnerSplits,
                        classes
                );

                controlSegments.add(segment);
            }
        }

        // Handle bidirectional merging if requested
        if (mergeBidirectional) {
            controlSegments = mergeBidirectionalSegments(controlSegments);
        }

        // Sort segments by control number (numerically if possible, otherwise lexicographically)
        controlSegments.sort((s1, s2) -> {
            int fromCompare = compareControlCodes(s1.getFromControl().value(), s2.getFromControl().value());
            if (fromCompare != 0) {
                return fromCompare;
            }
            return compareControlCodes(s1.getToControl().value(), s2.getToControl().value());
        });

        return controlSegments;
    }

    private List<ControlSegment> mergeBidirectionalSegments(List<ControlSegment> segments) {
        Map<String, ControlSegment> mergedSegmentMap = new HashMap<>();
        Set<String> processedPairs = new HashSet<>();

        for (ControlSegment segment : segments) {
            String fromControl = segment.getFromControl().value();
            String toControl = segment.getToControl().value();

            // Create sorted key for this segment pair
            String sortedKey = fromControl.compareTo(toControl) < 0 ?
                    fromControl + "-" + toControl :
                    toControl + "-" + fromControl;

            if (processedPairs.contains(sortedKey)) {
                continue; // Already merged
            }

            // Check if reverse segment exists
            Optional<ControlSegment> reverseSegment = segments.stream()
                    .filter(s -> s.getFromControl().value().equals(toControl) &&
                            s.getToControl().value().equals(fromControl))
                    .findFirst();

            if (reverseSegment.isPresent()) {
                // Merge forward and reverse segments
                List<RunnerSplit> mergedSplits = new ArrayList<>(segment.getRunnerSplits());
                mergedSplits.addAll(reverseSegment.get().getRunnerSplits());

                // Merge classes from both segments
                List<String> mergedClasses = new ArrayList<>(segment.getClasses());
                reverseSegment.get().getClasses().stream()
                        .filter(c -> !mergedClasses.contains(c))
                        .forEach(mergedClasses::add);
                mergedClasses.sort(String::compareTo);

                // Re-sort and recalculate positions
                mergedSplits.sort(Comparator.comparingDouble(RunnerSplit::getSplitTimeSeconds));

                List<RunnerSplit> recalculatedSplits = new ArrayList<>();
                Double leaderTime = mergedSplits.isEmpty() ? 0.0 : mergedSplits.getFirst().getSplitTimeSeconds();

                for (int i = 0; i < mergedSplits.size(); i++) {
                    RunnerSplit split = mergedSplits.get(i);
                    recalculatedSplits.add(new RunnerSplit(
                            split.getPersonId(),
                            split.getPersonName(),
                            split.getClassResultShortName(),
                            i + 1, // New position
                            split.getSplitTimeSeconds(),
                            split.getSplitTimeSeconds() - leaderTime
                    ));
                }

                ControlSegment mergedSegment = new ControlSegment(
                        segment.getFromControl(),
                        segment.getToControl(),
                        recalculatedSplits,
                        mergedClasses
                );

                mergedSegmentMap.put(sortedKey, mergedSegment);
                processedPairs.add(sortedKey);
            } else {
                // No reverse segment, keep as is
                mergedSegmentMap.put(sortedKey, segment);
                processedPairs.add(sortedKey);
            }
        }

        return new ArrayList<>(mergedSegmentMap.values());
    }

    /**
     * Adds virtual Start (punch_time = 0) and Finish (punch_time = runtime) controls
     * to the split times list.
     */
    private List<SplitTime> addStartAndFinishControls(
            List<SplitTime> splitTimes,
            PersonId personId,
            ClassResultShortName classShortName,
            RaceNumber raceNumber,
            Map<String, Double> runtimeMap) {

        List<SplitTime> extendedSplitTimes = new ArrayList<>();

        // Add virtual Start control with punch_time = 0
        extendedSplitTimes.add(SplitTime.of("S", 0.0));

        // Add all existing split times
        extendedSplitTimes.addAll(splitTimes);

        // Add virtual Finish control with punch_time = runtime (if available)
        String runtimeKey = makeRuntimeKey(personId, classShortName, raceNumber);
        Double runtime = runtimeMap.get(runtimeKey);
        if (runtime != null && runtime > 0) {
            extendedSplitTimes.add(SplitTime.of("F", runtime));
        }

        return extendedSplitTimes;
    }

    /**
     * Builds a map of runtime values for each person/class/race combination.
     * Key format: "personId-classShortName-raceNumber"
     */
    private Map<String, Double> buildRuntimeMap(ResultList resultList) {
        Map<String, Double> runtimeMap = new HashMap<>();

        if (resultList.getClassResults() == null) {
            return runtimeMap;
        }

        for (ClassResult classResult : resultList.getClassResults()) {
            for (PersonResult personResult : classResult.personResults().value()) {
                for (PersonRaceResult raceResult : personResult.personRaceResults().value()) {
                    if (raceResult.getRuntime().value() != null) {
                        String key = makeRuntimeKey(
                                personResult.personId(),
                                classResult.classResultShortName(),
                                raceResult.getRaceNumber()
                        );
                        runtimeMap.put(key, raceResult.getRuntime().value());
                    }
                }
            }
        }

        return runtimeMap;
    }

    /**
     * Creates a key for the runtime map.
     */
    private String makeRuntimeKey(PersonId personId, ClassResultShortName classShortName, RaceNumber raceNumber) {
        return personId.value() + "-" + classShortName.value() + "-" + raceNumber.value();
    }

    /**
     * Compares two control codes with special handling:
     * - "S" (Start) sorts first
     * - Numeric codes sort numerically
     * - Other codes sort lexicographically
     */
    private int compareControlCodes(String code1, String code2) {
        // Special handling for Start control
        boolean isStart1 = "S".equals(code1);
        boolean isStart2 = "S".equals(code2);

        if (isStart1 && isStart2) {
            return 0;
        }
        if (isStart1) {
            return -1; // S comes first
        }
        if (isStart2) {
            return 1; // S comes first
        }

        // Try numeric comparison for regular controls
        try {
            Integer num1 = Integer.parseInt(code1);
            Integer num2 = Integer.parseInt(code2);
            return num1.compareTo(num2);
        } catch (NumberFormatException e) {
            // If not numeric, compare lexicographically
            return code1.compareTo(code2);
        }
    }

    // Helper record for runner split data during calculation
    private record RunnerSplitData(
            PersonId personId,
            String personName,
            String classResultShortName,
            Double splitTimeSeconds
    ) {}
}
