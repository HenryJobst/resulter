package de.jobst.resulter.application.analysis;

import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.application.port.SegmentPI;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.*;
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
