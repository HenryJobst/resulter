package de.jobst.resulter.application.analysis;

import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.application.port.SegmentPI;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.analysis.MentalClassification;
import de.jobst.resulter.domain.analysis.MentalResilienceAnalysis;
import de.jobst.resulter.domain.analysis.PerformanceIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

class MentalResilienceServiceImplTest {

    SplitTimeListRepository splitTimeListRepository;
    ResultListRepository resultListRepository;
    SplitTimeAnalysisServiceImpl splitTimeAnalysisService;
    MentalResilienceServiceImpl service;

    ResultListId resultListId = ResultListId.of(1L);

    @BeforeEach
    void setUp() {
        splitTimeListRepository = mock(SplitTimeListRepository.class);
        resultListRepository = mock(ResultListRepository.class);
        splitTimeAnalysisService = mock(SplitTimeAnalysisServiceImpl.class);
        service = new MentalResilienceServiceImpl(splitTimeListRepository, resultListRepository, splitTimeAnalysisService);
    }

    @Test
    void analyzeMentalResilience_returnsEmpty_whenNoSplitTimeLists() {
        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of());

        MentalResilienceAnalysis result = service.analyzeMentalResilience(resultListId, List.of());

        assertThat(result.resultListId()).isEqualTo(resultListId);
        assertThat(result.runnerProfiles()).isEmpty();
        assertThat(result.statistics().totalRunners()).isEqualTo(0);
    }

    @Test
    void analyzeMentalResilience_throws_whenResultListNotFound() {
        SplitTimeList stl = splitTimeList("H21", 1L);
        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(stl));
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.empty());
        when(splitTimeAnalysisService.buildRuntimeMap(any())).thenReturn(Map.of());
        when(splitTimeAnalysisService.countRunnersPerClass(any())).thenReturn(Map.of());
        when(splitTimeAnalysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());

        assertThatThrownBy(() -> service.analyzeMentalResilience(resultListId, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Result list not found");
    }

    @Test
    void analyzeMentalResilience_skipsRunners_belowClassThreshold() {
        SplitTimeList stl = splitTimeList("H21", 1L);
        ResultList resultList = emptyResultList();

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(stl));
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(splitTimeAnalysisService.buildRuntimeMap(resultList)).thenReturn(Map.of());
        // countRunnersPerClass returns 2 for H21 → below MIN_RUNNERS_PER_CLASS_FOR_ANALYSIS (3)
        when(splitTimeAnalysisService.countRunnersPerClass(any())).thenReturn(Map.of("H21", 2));
        when(splitTimeAnalysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());

        MentalResilienceAnalysis result = service.analyzeMentalResilience(resultListId, List.of());

        assertThat(result.runnerProfiles()).isEmpty();
        assertThat(result.statistics().totalRunners()).isEqualTo(1);
    }

    @Test
    void analyzeMentalResilience_filtersPersonIds_whenFilterGiven() {
        SplitTimeList stl1 = splitTimeList("H21", 1L);
        SplitTimeList stl2 = splitTimeList("H21", 2L);
        ResultList resultList = emptyResultList();

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(stl1, stl2));
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(splitTimeAnalysisService.buildRuntimeMap(resultList)).thenReturn(Map.of());
        when(splitTimeAnalysisService.countRunnersPerClass(any())).thenReturn(Map.of("H21", 5));
        when(splitTimeAnalysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());
        // Segment times empty → analyzeRunner returns empty
        when(splitTimeAnalysisService.calculateSegmentTimes(any(), any())).thenReturn(List.of());

        // Filter to only person 1
        MentalResilienceAnalysis result = service.analyzeMentalResilience(resultListId, List.of(1L));

        // Both runners are in list, but only person 1 passes filter — both return empty profiles
        assertThat(result.runnerProfiles()).isEmpty();
        verify(splitTimeAnalysisService, times(1)).calculateSegmentTimes(eq(stl1), any());
        verify(splitTimeAnalysisService, never()).calculateSegmentTimes(eq(stl2), any());
    }

    @Test
    void analyzeMentalResilience_returnsProfilesWithMistakes_whenFoundFromMockedData() {
        SplitTimeList stl = splitTimeList("H21", 1L);
        ResultList resultList = emptyResultList();

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(stl));
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(splitTimeAnalysisService.buildRuntimeMap(resultList)).thenReturn(Map.of());
        // 5 runners → above threshold
        when(splitTimeAnalysisService.countRunnersPerClass(any())).thenReturn(Map.of("H21", 5));
        when(splitTimeAnalysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());

        // Segment times: 5 segments to trigger normalPI calculation
        List<SegmentTime> segments = List.of(
                new SegmentTime(0, "S", "31", 100.0),
                new SegmentTime(1, "31", "32", 100.0),
                new SegmentTime(2, "32", "33", 100.0),
                new SegmentTime(3, "33", "34", 100.0),
                new SegmentTime(4, "34", "F", 100.0)
        );
        when(splitTimeAnalysisService.calculateSegmentTimes(stl, Map.of())).thenReturn(segments);

        // PIs for segments: all at 1.1 (no mistakes)
        List<SegmentPI> segmentPIs = List.of(
                new SegmentPI(0, "S", "31", 110.0, 100.0, new PerformanceIndex(1.1)),
                new SegmentPI(1, "31", "32", 110.0, 100.0, new PerformanceIndex(1.1)),
                new SegmentPI(2, "32", "33", 110.0, 100.0, new PerformanceIndex(1.1)),
                new SegmentPI(3, "33", "34", 110.0, 100.0, new PerformanceIndex(1.1)),
                new SegmentPI(4, "34", "F", 110.0, 100.0, new PerformanceIndex(1.1))
        );
        when(splitTimeAnalysisService.calculateSegmentPIs(any(), any(), eq("H21"))).thenReturn(segmentPIs);
        // normalPI: return null → not enough non-mistake segments (triggers Optional.empty())
        when(splitTimeAnalysisService.calculateNormalPI(any())).thenReturn(null);

        MentalResilienceAnalysis result = service.analyzeMentalResilience(resultListId, List.of());

        // No profiles with mistakes, but runner was analyzed
        assertThat(result.runnerProfiles()).isEmpty();
    }

    @Test
    void analyzeMentalResilience_returnsProfile_withPanicClassification() {
        SplitTimeList stl = splitTimeList("H21", 1L);
        ResultList resultList = emptyResultList();

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(stl));
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(splitTimeAnalysisService.buildRuntimeMap(resultList)).thenReturn(Map.of());
        when(splitTimeAnalysisService.countRunnersPerClass(any())).thenReturn(Map.of("H21", 5));
        when(splitTimeAnalysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());

        // 3 segments: seg0 (mistake), seg1 (reaction, PANIC pi=0.8), seg2 (last, not mistake)
        SegmentPI seg0 = new SegmentPI(0, "31", "32", 500.0, 100.0, new PerformanceIndex(5.0));
        SegmentPI seg1 = new SegmentPI(1, "32", "33", 80.0, 100.0, new PerformanceIndex(0.8));
        SegmentPI seg2 = new SegmentPI(2, "33", "F", 100.0, 100.0, new PerformanceIndex(1.0));

        when(splitTimeAnalysisService.calculateSegmentTimes(any(), any())).thenReturn(List.of(
                new SegmentTime(0, "31", "32", 500.0),
                new SegmentTime(1, "32", "33", 80.0),
                new SegmentTime(2, "33", "F", 100.0)
        ));
        when(splitTimeAnalysisService.calculateSegmentPIs(any(), any(), eq("H21")))
                .thenReturn(List.of(seg0, seg1, seg2));
        when(splitTimeAnalysisService.calculateNormalPI(any())).thenReturn(new PerformanceIndex(1.0));
        // seg0 is a mistake, seg1 is not (not chain error)
        when(splitTimeAnalysisService.isMistake(eq(seg0), anyDouble())).thenReturn(true);
        when(splitTimeAnalysisService.isMistake(eq(seg1), anyDouble())).thenReturn(false);
        // last segment check: seg2 is not a mistake
        when(splitTimeAnalysisService.isMistakeBase(eq(seg2), anyDouble()))
                .thenReturn(new MistakeResult(0.0, 0.0, false));
        when(splitTimeAnalysisService.calculateMedian(any())).thenReturn(-0.2);

        MentalResilienceAnalysis result = service.analyzeMentalResilience(resultListId, List.of());

        assertThat(result.runnerProfiles()).hasSize(1);
        assertThat(result.runnerProfiles().getFirst().classification()).isEqualTo(MentalClassification.PANIC);
        assertThat(result.statistics().totalRunners()).isEqualTo(1);
        assertThat(result.statistics().runnersWithMistakes()).isEqualTo(1);
        assertThat(result.statistics().panicReactions()).isEqualTo(1);
    }

    @Test
    void analyzeMentalResilience_returnsProfile_withChainErrorClassification() {
        SplitTimeList stl = splitTimeList("H21", 1L);
        ResultList resultList = emptyResultList();

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(stl));
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(splitTimeAnalysisService.buildRuntimeMap(resultList)).thenReturn(Map.of());
        when(splitTimeAnalysisService.countRunnersPerClass(any())).thenReturn(Map.of("H21", 5));
        when(splitTimeAnalysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());

        SegmentPI seg0 = new SegmentPI(0, "31", "32", 500.0, 100.0, new PerformanceIndex(5.0));
        SegmentPI seg1 = new SegmentPI(1, "32", "33", 500.0, 100.0, new PerformanceIndex(5.0));
        SegmentPI seg2 = new SegmentPI(2, "33", "F", 100.0, 100.0, new PerformanceIndex(1.0));

        when(splitTimeAnalysisService.calculateSegmentTimes(any(), any())).thenReturn(List.of(
                new SegmentTime(0, "31", "32", 500.0),
                new SegmentTime(1, "32", "33", 500.0),
                new SegmentTime(2, "33", "F", 100.0)
        ));
        when(splitTimeAnalysisService.calculateSegmentPIs(any(), any(), eq("H21")))
                .thenReturn(List.of(seg0, seg1, seg2));
        when(splitTimeAnalysisService.calculateNormalPI(any())).thenReturn(new PerformanceIndex(1.0));
        // Both seg0 and seg1 are mistakes → chain error
        when(splitTimeAnalysisService.isMistake(eq(seg0), anyDouble())).thenReturn(true);
        when(splitTimeAnalysisService.isMistake(eq(seg1), anyDouble())).thenReturn(true);
        when(splitTimeAnalysisService.isMistakeBase(eq(seg2), anyDouble()))
                .thenReturn(new MistakeResult(0.0, 0.0, false));
        when(splitTimeAnalysisService.calculateMedian(any())).thenReturn(null);

        MentalResilienceAnalysis result = service.analyzeMentalResilience(resultListId, List.of());

        assertThat(result.runnerProfiles()).hasSize(1);
        assertThat(result.runnerProfiles().getFirst().mistakeReactions().getFirst().classification())
                .isEqualTo(MentalClassification.CHAIN_ERROR);
    }

    @Test
    void analyzeMentalResilience_skipsReaction_whenNextSegmentIsFinal() {
        SplitTimeList stl = splitTimeList("H21", 1L);
        ResultList resultList = emptyResultList();

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(stl));
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(splitTimeAnalysisService.buildRuntimeMap(resultList)).thenReturn(Map.of());
        when(splitTimeAnalysisService.countRunnersPerClass(any())).thenReturn(Map.of("H21", 5));
        when(splitTimeAnalysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());

        // 2 segments: seg0 (mistake), seg1 (next = final "F") → reaction skipped
        SegmentPI seg0 = new SegmentPI(0, "31", "32", 500.0, 100.0, new PerformanceIndex(5.0));
        SegmentPI seg1 = new SegmentPI(1, "32", "F", 50.0, 50.0, new PerformanceIndex(1.0));

        when(splitTimeAnalysisService.calculateSegmentTimes(any(), any())).thenReturn(List.of(
                new SegmentTime(0, "31", "32", 500.0),
                new SegmentTime(1, "32", "F", 50.0)
        ));
        when(splitTimeAnalysisService.calculateSegmentPIs(any(), any(), eq("H21")))
                .thenReturn(List.of(seg0, seg1));
        when(splitTimeAnalysisService.calculateNormalPI(any())).thenReturn(new PerformanceIndex(1.0));
        when(splitTimeAnalysisService.isMistake(eq(seg0), anyDouble())).thenReturn(true);
        // seg1 is final code → reaction skipped
        when(splitTimeAnalysisService.isMistakeBase(eq(seg1), anyDouble()))
                .thenReturn(new MistakeResult(0.0, 0.0, false));
        when(splitTimeAnalysisService.calculateMedian(any())).thenReturn(null);

        MentalResilienceAnalysis result = service.analyzeMentalResilience(resultListId, List.of());

        // Reaction skipped → no profiles with mistakes
        assertThat(result.runnerProfiles()).isEmpty();
    }

    @Test
    void analyzeMentalResilience_detectsLastSegmentMistake() {
        SplitTimeList stl = splitTimeList("H21", 1L);
        ResultList resultList = emptyResultList();

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(stl));
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(splitTimeAnalysisService.buildRuntimeMap(resultList)).thenReturn(Map.of());
        when(splitTimeAnalysisService.countRunnersPerClass(any())).thenReturn(Map.of("H21", 5));
        when(splitTimeAnalysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());

        // 3 segments: seg0 (not mistake), seg1 (not mistake), seg2 (last = mistake)
        SegmentPI seg0 = new SegmentPI(0, "S", "31", 100.0, 100.0, new PerformanceIndex(1.0));
        SegmentPI seg1 = new SegmentPI(1, "31", "32", 100.0, 100.0, new PerformanceIndex(1.0));
        SegmentPI seg2 = new SegmentPI(2, "32", "F", 500.0, 100.0, new PerformanceIndex(5.0));

        when(splitTimeAnalysisService.calculateSegmentTimes(any(), any())).thenReturn(List.of(
                new SegmentTime(0, "S", "31", 100.0),
                new SegmentTime(1, "31", "32", 100.0),
                new SegmentTime(2, "32", "F", 500.0)
        ));
        when(splitTimeAnalysisService.calculateSegmentPIs(any(), any(), eq("H21")))
                .thenReturn(List.of(seg0, seg1, seg2));
        when(splitTimeAnalysisService.calculateNormalPI(any())).thenReturn(new PerformanceIndex(1.0));
        when(splitTimeAnalysisService.isMistake(any(SegmentPI.class), anyDouble())).thenReturn(false);
        // Last segment IS a mistake → logged but no reaction available
        when(splitTimeAnalysisService.isMistakeBase(eq(seg2), anyDouble()))
                .thenReturn(new MistakeResult(100.0, 400.0, true));
        when(splitTimeAnalysisService.calculateMedian(any())).thenReturn(null);

        MentalResilienceAnalysis result = service.analyzeMentalResilience(resultListId, List.of());

        // Last segment mistake logged but not added to profiles (no reaction)
        assertThat(result.runnerProfiles()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private SplitTimeList splitTimeList(String className, long personId) {
        return new SplitTimeList(
                SplitTimeListId.empty(),
                EventId.of(1L),
                resultListId,
                ClassResultShortName.of(className),
                PersonId.of(personId),
                RaceNumber.of((byte) 1),
                List.of()
        );
    }

    private ResultList emptyResultList() {
        return new ResultList(resultListId, EventId.of(1L), RaceId.of(1L), null, null, null, List.of());
    }
}
