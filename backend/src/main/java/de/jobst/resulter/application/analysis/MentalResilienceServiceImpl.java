package de.jobst.resulter.application.analysis;

import de.jobst.resulter.application.port.MentalResilienceService;
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

/**
 * Implementation of Mental Resilience Index analysis service.
 *
 * <p>Calculates how runners mentally react after making navigation mistakes by analyzing
 * their pace changes on segments following errors.</p>
 */
@Service
@Slf4j
public class MentalResilienceServiceImpl implements MentalResilienceService {

    private final SplitTimeListRepository splitTimeListRepository;
    private final ResultListRepository resultListRepository;
    private final SplitTimeAnalysisServiceImpl splitTimeAnalysisServiceImpl;

    public MentalResilienceServiceImpl(
        SplitTimeListRepository splitTimeListRepository, ResultListRepository resultListRepository,
        SplitTimeAnalysisServiceImpl splitTimeAnalysisServiceImpl) {
        this.splitTimeListRepository = splitTimeListRepository;
        this.resultListRepository = resultListRepository;
        this.splitTimeAnalysisServiceImpl = splitTimeAnalysisServiceImpl;
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
        Map<RuntimeKey, Double> runtimeMap = splitTimeAnalysisServiceImpl.buildRuntimeMap(resultList);
        log.debug("Built runtime map with {} entries", runtimeMap.size());

        // Step 5: Count runners per class
        Map<String, Integer> runnersPerClass = splitTimeAnalysisServiceImpl.countRunnersPerClass(splitTimeLists);
        log.debug("Runner count per class: {}", runnersPerClass);

        // Step 6: Calculate reference times per segment
        Map<SegmentKey, Double> referenceTimesPerSegment =
            splitTimeAnalysisServiceImpl.calculateReferenceTimesPerSegment(splitTimeLists, runtimeMap);
        log.debug("Calculated reference times for {} segments", referenceTimesPerSegment.size());

        // Step 7: Analyze each or only filtered runners
        Set<Long> filterPersonIdSet = new HashSet<>(filterPersonIds);

        // define the predicate (filter condition) separately
        Predicate<SplitTimeList> personIdFilter = stl ->
            filterPersonIdSet.isEmpty() || filterPersonIdSet.contains(stl.getPersonId().value());

        // define the analysis function
        Function<SplitTimeList, Optional<RunnerMentalProfile>> runnerAnalyzer = stl ->
            analyzeRunner(stl, referenceTimesPerSegment, runtimeMap, runnersPerClass);

        List<RunnerMentalProfile> runnerProfiles = splitTimeLists.stream()
            .filter(personIdFilter)
            .map(runnerAnalyzer)
            .flatMap(Optional::stream)
            .filter(RunnerMentalProfile::hasMistakes)
            .toList();

        log.debug("Analyzed {} runners with mistakes", runnerProfiles.size());

        // Step 8: Calculate aggregate statistics
        MriStatistics statistics = calculateStatistics(splitTimeLists.size(), runnerProfiles);

        if (log.isInfoEnabled()) {
            log.info("Mental resilience analysis completed in {} ms: {} runners, {} with mistakes, {} total mistakes",
                System.currentTimeMillis() - startTime,
                statistics.totalRunners(),
                statistics.runnersWithMistakes(),
                statistics.totalMistakes());
        }

        return new MentalResilienceAnalysis(resultListId, eventId, runnerProfiles, statistics);
    }


    /**
     * Analyzes a single runner and creates their mental profile.
     * Returns Optional.empty() if the class has too few runners for reliable analysis.
     */
    private Optional<RunnerMentalProfile> analyzeRunner(
            SplitTimeList splitTimeList,
            Map<SegmentKey, Double> referenceTimesPerSegment,
            Map<RuntimeKey, Double> runtimeMap,
            Map<String, Integer> runnersPerClass) {

        PersonId personId = splitTimeList.getPersonId();
        String className = splitTimeList.getClassResultShortName().value();
        RaceNumber raceNumber = splitTimeList.getRaceNumber();

        // Check minimum runner threshold
        int classRunnerCount = runnersPerClass.getOrDefault(className, 0);
        if (classRunnerCount < SplitTimeAnalysisServiceImpl.MIN_RUNNERS_PER_CLASS_FOR_ANALYSIS) {
            log.info("Skipping MRI for runner {} in class {} - only {} runners (min: {})",
                    personId, className, classRunnerCount, SplitTimeAnalysisServiceImpl.MIN_RUNNERS_PER_CLASS_FOR_ANALYSIS);
            return Optional.empty();
        }

        // Calculate segment times
        List<SegmentTime> segmentTimes = splitTimeAnalysisServiceImpl.calculateSegmentTimes(splitTimeList, runtimeMap);

        if (segmentTimes.isEmpty()) {
            log.debug("No valid segments for runner {}", personId);
            return Optional.empty();
        }

        // Calculate Performance Index for each segment
        List<SegmentPI> segmentPIs =
            splitTimeAnalysisServiceImpl.calculateSegmentPIs(segmentTimes, referenceTimesPerSegment, className);

        // Calculate Normal PI (average excluding mistakes)
        PerformanceIndex normalPI = splitTimeAnalysisServiceImpl.calculateNormalPI(segmentPIs);

        // Check if we have enough non-mistake segments to establish a baseline
        if (normalPI == null) {
            log.info("Skipping MRI for runner {} in class {} - too many mistakes (< {} non-mistake segments)",
                    personId, className, SplitTimeAnalysisServiceImpl.MIN_NON_MISTAKE_SEGMENTS);
            return Optional.empty();
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
            return Optional.empty();
        }

        // Calculate average MRI
        double averageMRI = mistakeReactions.stream()
                // skip chain error on calculating averageMRI
                .filter(pair -> !pair.classification().equals(MentalClassification.CHAIN_ERROR))
                .mapToDouble(pair -> pair.mri().value())
                .average()
                .orElse(0.0);

        // Classify based on average MRI
        MentalClassification classification = MentalResilienceIndex.of(
                new PerformanceIndex(normalPI.value() + averageMRI),
                normalPI
        ).classify();

        boolean reliableData = classRunnerCount >= SplitTimeAnalysisServiceImpl.RELIABLE_RUNNERS_THRESHOLD;

        return Optional.of(new RunnerMentalProfile(
                personId,
                className,
                raceNumber,
                classRunnerCount,
                reliableData,
                normalPI,
                mistakeReactions,
                averageMRI,
                classification
        ));
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

            boolean isMistake = splitTimeAnalysisServiceImpl.isMistake(currentSegment, medianDifferencePercent);

            if (isMistake) {
                SegmentPI nextSegment = segmentPIs.get(i + 1);

                if (nextSegment.toControl().equals(SplitTimeAnalysisServiceImpl.FINAL_CODE)) {
                    // skip reactions on last segment
                    // most of the time it is short and easy so runners tend to give all they have
                    continue;
                }

                // Calculate MRI
                MentalResilienceIndex mri = MentalResilienceIndex.of(nextSegment.pi(), normalPI);

                // Check if reaction segment is also a mistake (chain error)
                boolean nextIsMistake = splitTimeAnalysisServiceImpl.isMistake(nextSegment, medianDifferencePercent
                );

                MentalClassification classification;
                String currentSegmentPiFormated = String.format("%.3f", currentSegment.pi().value());
                if (nextIsMistake) {
                    classification = MentalClassification.CHAIN_ERROR;
                    log.debug(
                        "Detected CHAIN ERROR at leg {}: mistake PI={}, reaction PI={} (also a mistake)",
                        currentSegment.legNumber(), currentSegmentPiFormated,
                        String.format("%.3f", nextSegment.pi().value()));

                } else {
                    classification = mri.classify();
                    log.debug("Detected mistake at leg {}: PI={}, reaction MRI={} ({})", currentSegment.legNumber(),
                        currentSegmentPiFormated,
                            String.format("%.3f", mri.value()),
                            classification);
                }

                // Create mistake-reaction pair
                MistakeReactionPair pair = new MistakeReactionPair(currentSegment.legNumber(),
                        new ControlCode(currentSegment.fromControl()),
                        new ControlCode(currentSegment.toControl()), currentSegment.pi(), nextSegment.legNumber(),
                        new ControlCode(nextSegment.fromControl()),
                        new ControlCode(nextSegment.toControl()), nextSegment.pi(),
                        mri,
                        classification
                );

                mistakeReactions.add(pair);
            }
        }

        // Check last segment for mistake (no reaction available)
        if (!segmentPIs.isEmpty()) {
            MistakeResult lastMistakeResult = splitTimeAnalysisServiceImpl.isMistakeBase(segmentPIs.getLast(), medianDifferencePercent);
            if (lastMistakeResult.isMistake()) {
                log.debug("Last segment mistake detected (diff={}%, time loss={}s) but skipped (no reaction segment available)",
                        String.format("%.1f", lastMistakeResult.diffPercent()),
                        String.format("%.1f", lastMistakeResult.timeLossSeconds()));
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

        Double medianMRI = splitTimeAnalysisServiceImpl.calculateMedian(allMRIs);

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

    private MentalResilienceAnalysis createEmptyAnalysis(ResultListId resultListId) {
        EventId eventId = new EventId(0L); // Placeholder, will be overridden if data exists
        MriStatistics emptyStats = new MriStatistics(0, 0, 0, 0, 0, 0, null, null);
        return new MentalResilienceAnalysis(resultListId, eventId, Collections.emptyList(), emptyStats);
    }

}
