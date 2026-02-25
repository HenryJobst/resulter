package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.application.port.AnomalyDetectionService;
import de.jobst.resulter.application.port.HangingDetectionService;
import de.jobst.resulter.application.port.MentalResilienceService;
import de.jobst.resulter.application.port.SplitTimeRankingService;
import de.jobst.resulter.application.port.SplitTimeTableService;
import de.jobst.resulter.domain.ClassResultShortName;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.ResultListId;
import de.jobst.resulter.domain.analysis.SplitTimeAnalysis;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SplitTimeAnalysisControllerTest {

    @Test
    void analyzeSplitTimesRanking_shouldForwardSequenceOptions() {
        SplitTimeRankingService splitTimeRankingService = mock(SplitTimeRankingService.class);
        SplitTimeAnalysisController controller = new SplitTimeAnalysisController(
                splitTimeRankingService,
                mock(MentalResilienceService.class),
                mock(AnomalyDetectionService.class),
                mock(HangingDetectionService.class),
                mock(SplitTimeTableService.class)
        );

        when(splitTimeRankingService.analyzeSplitTimesRanking(any(ResultListId.class), any(Boolean.class), any(), any(Boolean.class), any(Boolean.class), any(Integer.class)))
                .thenReturn(List.of());

        controller.analyzeSplitTimesRanking(123L, false, List.of(1L, 2L), true, true, 4);

        verify(splitTimeRankingService).analyzeSplitTimesRanking(
                ResultListId.of(123L),
                false,
                List.of(1L, 2L),
                true,
                true,
                4
        );
    }

    @Test
    void analyzeSplitTimesRanking_shouldUseDefaultSequenceOptions() {
        SplitTimeRankingService splitTimeRankingService = mock(SplitTimeRankingService.class);
        SplitTimeAnalysisController controller = new SplitTimeAnalysisController(
                splitTimeRankingService,
                mock(MentalResilienceService.class),
                mock(AnomalyDetectionService.class),
                mock(HangingDetectionService.class),
                mock(SplitTimeTableService.class)
        );

        when(splitTimeRankingService.analyzeSplitTimesRanking(any(ResultListId.class), any(Boolean.class), any(), any(Boolean.class), any(Boolean.class), any(Integer.class)))
                .thenReturn(List.of(new SplitTimeAnalysis(
                        ResultListId.of(123L),
                        EventId.of(1L),
                        ClassResultShortName.of("Alle Klassen"),
                        List.of(),
                        List.of()
                )));

        controller.analyzeSplitTimesRanking(123L, null, null, null, null, null);

        verify(splitTimeRankingService).analyzeSplitTimesRanking(
                ResultListId.of(123L),
                false,
                List.of(),
                false,
                false,
                3
        );
    }
}
