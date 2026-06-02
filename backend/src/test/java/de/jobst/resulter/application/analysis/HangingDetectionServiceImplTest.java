package de.jobst.resulter.application.analysis;

import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.analysis.HangingAnalysis;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HangingDetectionServiceImplTest {

    ResultListId resultListId = ResultListId.of(1L);

    @Test
    void analyzeHanging_returnsEmpty_whenAllDependenciesNull() {
        HangingDetectionServiceImpl service = new HangingDetectionServiceImpl(null, null, null);

        HangingAnalysis result = service.analyzeHanging(resultListId, List.of());

        assertThat(result.resultListId()).isEqualTo(resultListId);
        assertThat(result.runnerProfiles()).isEmpty();
        assertThat(result.statistics().totalRunners()).isEqualTo(0);
    }

    @Test
    void analyzeHanging_returnsEmpty_whenNoSplitTimeLists() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl analysisService = mock(SplitTimeAnalysisServiceImpl.class);
        HangingDetectionServiceImpl service = new HangingDetectionServiceImpl(stlRepo, rlRepo, analysisService);

        when(stlRepo.findByResultListId(resultListId)).thenReturn(List.of());

        HangingAnalysis result = service.analyzeHanging(resultListId, List.of());

        assertThat(result.resultListId()).isEqualTo(resultListId);
        assertThat(result.runnerProfiles()).isEmpty();
    }

    @Test
    void analyzeHanging_throws_whenResultListNotFound() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl analysisService = mock(SplitTimeAnalysisServiceImpl.class);
        HangingDetectionServiceImpl service = new HangingDetectionServiceImpl(stlRepo, rlRepo, analysisService);

        SplitTimeList stl = splitTimeList("H21", 1L);
        when(stlRepo.findByResultListId(resultListId)).thenReturn(List.of(stl));
        when(rlRepo.findById(resultListId)).thenReturn(Optional.empty());
        when(analysisService.buildRuntimeMap(any())).thenReturn(Map.of());
        when(analysisService.countRunnersPerClass(any())).thenReturn(Map.of());
        when(analysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());
        when(analysisService.calculateSegmentTimes(any(), any())).thenReturn(List.of());
        when(analysisService.calculateSegmentPIs(any(), any(), any())).thenReturn(List.of());

        assertThatThrownBy(() -> service.analyzeHanging(resultListId, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Result list not found");
    }

    @Test
    void analyzeHanging_skipsRunner_belowClassThreshold() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl analysisService = mock(SplitTimeAnalysisServiceImpl.class);
        HangingDetectionServiceImpl service = new HangingDetectionServiceImpl(stlRepo, rlRepo, analysisService);

        SplitTimeList stl = splitTimeList("H21", 1L);
        ResultList resultList = emptyResultList();

        when(stlRepo.findByResultListId(resultListId)).thenReturn(List.of(stl));
        when(rlRepo.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(analysisService.buildRuntimeMap(resultList)).thenReturn(Map.of());
        // Only 2 runners → below MIN_RUNNERS_PER_CLASS_FOR_ANALYSIS (3)
        when(analysisService.countRunnersPerClass(any())).thenReturn(Map.of("H21", 2));
        when(analysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());
        when(analysisService.calculateSegmentTimes(any(), any())).thenReturn(List.of());
        when(analysisService.calculateSegmentPIs(any(), any(), any())).thenReturn(List.of());

        HangingAnalysis result = service.analyzeHanging(resultListId, List.of());

        // Runner skipped → no profile returned but analysis created
        assertThat(result.runnerProfiles()).isEmpty();
        assertThat(result.statistics().totalRunners()).isEqualTo(1);
    }

    @Test
    void analyzeHanging_filtersPersonIds() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl analysisService = mock(SplitTimeAnalysisServiceImpl.class);
        HangingDetectionServiceImpl service = new HangingDetectionServiceImpl(stlRepo, rlRepo, analysisService);

        SplitTimeList stl1 = splitTimeList("H21", 1L);
        SplitTimeList stl2 = splitTimeList("H21", 2L);
        ResultList resultList = emptyResultList();

        when(stlRepo.findByResultListId(resultListId)).thenReturn(List.of(stl1, stl2));
        when(rlRepo.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(analysisService.buildRuntimeMap(resultList)).thenReturn(Map.of());
        when(analysisService.countRunnersPerClass(any())).thenReturn(Map.of("H21", 5));
        when(analysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());
        when(analysisService.calculateSegmentTimes(any(), any())).thenReturn(List.of());
        when(analysisService.calculateSegmentPIs(any(), any(), any())).thenReturn(List.of());

        // Filter: only person 1
        HangingAnalysis result = service.analyzeHanging(resultListId, List.of(1L));

        // preComputeAllSegmentPIs runs on both runners (2 calls),
        // analyzeRunner runs only for person 1 (1 additional call) → 3 total
        verify(analysisService, times(3)).calculateSegmentTimes(any(), any());
        // All runners are returned (no filter in profile list for HangingDetection)
        assertThat(result.statistics().totalRunners()).isEqualTo(2);
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
