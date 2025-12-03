package de.jobst.resulter.application;

import de.jobst.resulter.application.port.PersonRepository;
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

    private final SplitTimeListRepository splitTimeListRepository;
    private final PersonRepository personRepository;

    public SplitTimeAnalysisServiceImpl(
            SplitTimeListRepository splitTimeListRepository,
            PersonRepository personRepository) {
        this.splitTimeListRepository = splitTimeListRepository;
        this.personRepository = personRepository;
    }

    @Override
    public Optional<SplitTimeAnalysis> analyzeSplitTimesRanking(
            ResultListId resultListId,
            boolean mergeBidirectional,
            List<String> filterNames) {

        // Fetch all split time lists for the result list
        List<SplitTimeList> splitTimeLists = splitTimeListRepository.findByResultListId(resultListId);

        if (splitTimeLists.isEmpty()) {
            log.debug("No split time data found for result list {}", resultListId);
            return Optional.empty();
        }

        // Get person IDs and fetch person names
        Set<PersonId> personIds = splitTimeLists.stream()
                .map(SplitTimeList::getPersonId)
                .collect(Collectors.toSet());

        Map<PersonId, Person> personMap = personRepository.findAllById(personIds);

        // Group by class result short name
        // For simplicity, we'll use the first class we find
        // TODO: In a future enhancement, support multiple classes
        ClassResultShortName classShortName = splitTimeLists.get(0).getClassResultShortName();
        EventId eventId = splitTimeLists.get(0).getEventId();

        // Filter to single class
        List<SplitTimeList> classSplitTimeLists = splitTimeLists.stream()
                .filter(stl -> stl.getClassResultShortName().equals(classShortName))
                .toList();

        // Calculate control segments
        List<ControlSegment> controlSegments = calculateControlSegments(
                classSplitTimeLists,
                personMap,
                mergeBidirectional,
                filterNames
        );

        SplitTimeAnalysis analysis = new SplitTimeAnalysis(
                resultListId,
                eventId,
                classShortName,
                controlSegments
        );

        return Optional.of(analysis);
    }

    private List<ControlSegment> calculateControlSegments(
            List<SplitTimeList> splitTimeLists,
            Map<PersonId, Person> personMap,
            boolean mergeBidirectional,
            List<String> filterNames) {

        // Build a map of control sequences
        Map<String, Map<String, List<RunnerSplitData>>> segmentMap = new HashMap<>();

        for (SplitTimeList splitTimeList : splitTimeLists) {
            List<SplitTime> splitTimes = splitTimeList.getSplitTimes();

            // Sort split times by punch time to ensure chronological order
            List<SplitTime> sortedSplitTimes = new ArrayList<>(splitTimes);
            sortedSplitTimes.sort(Comparator.comparing(st -> st.getPunchTime().value()));

            // Calculate split times between consecutive controls
            for (int i = 1; i < sortedSplitTimes.size(); i++) {
                SplitTime previousSplit = sortedSplitTimes.get(i - 1);
                SplitTime currentSplit = sortedSplitTimes.get(i);

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
                if (filterNames != null && !filterNames.isEmpty()) {
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

                // Calculate positions and time behind leader
                List<RunnerSplit> runnerSplits = new ArrayList<>();
                Double leaderTime = runnerData.isEmpty() ? 0.0 : runnerData.get(0).splitTimeSeconds();

                for (int i = 0; i < runnerData.size(); i++) {
                    RunnerSplitData data = runnerData.get(i);
                    int position = i + 1;
                    Double timeBehind = data.splitTimeSeconds() - leaderTime;

                    runnerSplits.add(new RunnerSplit(
                            data.personId(),
                            data.personName(),
                            position,
                            data.splitTimeSeconds(),
                            timeBehind
                    ));
                }

                ControlSegment segment = new ControlSegment(
                        ControlCode.of(fromControl),
                        ControlCode.of(toControl),
                        runnerSplits
                );

                controlSegments.add(segment);
            }
        }

        // Handle bidirectional merging if requested
        if (mergeBidirectional) {
            controlSegments = mergeBidirectionalSegments(controlSegments);
        }

        return controlSegments;
    }

    private List<ControlSegment> mergeBidirectionalSegments(List<ControlSegment> segments) {
        Map<String, ControlSegment> mergedSegmentMap = new HashMap<>();
        Set<String> processedPairs = new HashSet<>();

        for (ControlSegment segment : segments) {
            String fromControl = segment.getFromControl().value();
            String toControl = segment.getToControl().value();

            // Create sorted key for this segment pair
            String forwardKey = fromControl + "->" + toControl;
            String reverseKey = toControl + "->" + fromControl;
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

                // Re-sort and recalculate positions
                mergedSplits.sort(Comparator.comparingDouble(RunnerSplit::getSplitTimeSeconds));

                List<RunnerSplit> recalculatedSplits = new ArrayList<>();
                Double leaderTime = mergedSplits.isEmpty() ? 0.0 : mergedSplits.get(0).getSplitTimeSeconds();

                for (int i = 0; i < mergedSplits.size(); i++) {
                    RunnerSplit split = mergedSplits.get(i);
                    recalculatedSplits.add(new RunnerSplit(
                            split.getPersonId(),
                            split.getPersonName(),
                            i + 1, // New position
                            split.getSplitTimeSeconds(),
                            split.getSplitTimeSeconds() - leaderTime
                    ));
                }

                ControlSegment mergedSegment = new ControlSegment(
                        segment.getFromControl(),
                        segment.getToControl(),
                        recalculatedSplits
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

    // Helper record for runner split data during calculation
    private record RunnerSplitData(
            PersonId personId,
            String personName,
            Double splitTimeSeconds
    ) {}
}
