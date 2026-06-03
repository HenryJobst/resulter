package de.jobst.resulter.adapter.driver.web;

import de.jobst.resulter.application.port.AnomalyDetectionService;
import de.jobst.resulter.application.port.ClassGroupOption;
import de.jobst.resulter.application.port.CourseGroupOption;
import de.jobst.resulter.application.port.HangingDetectionService;
import de.jobst.resulter.application.port.MentalResilienceService;
import de.jobst.resulter.application.port.SplitTimeRankingService;
import de.jobst.resulter.application.port.SplitTimeTableService;
import de.jobst.resulter.domain.ClassResultShortName;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.ResultListId;
import de.jobst.resulter.domain.analysis.AnomalyAnalysis;
import de.jobst.resulter.domain.analysis.HangingAnalysis;
import de.jobst.resulter.domain.analysis.HangingStatistics;
import de.jobst.resulter.domain.analysis.MentalResilienceAnalysis;
import de.jobst.resulter.domain.analysis.MriStatistics;
import de.jobst.resulter.domain.analysis.SplitTimeAnalysis;
import de.jobst.resulter.domain.analysis.SplitTimeTable;
import de.jobst.resulter.domain.analysis.SplitTimeTableMetadata;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Test
    void getPersonsForResultList_shouldReturnEmptyList() {
        SplitTimeRankingService rankingService = mock(SplitTimeRankingService.class);
        SplitTimeAnalysisController controller = controller(rankingService, null, null, null, null);

        when(rankingService.getPersonsForResultList(ResultListId.of(5L))).thenReturn(List.of());

        var result = controller.getPersonsForResultList(5L);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isEmpty();
        verify(rankingService).getPersonsForResultList(ResultListId.of(5L));
    }

    @Test
    void analyzeMentalResilience_shouldReturnOk_whenNoMistakes() {
        MentalResilienceService mentalService = mock(MentalResilienceService.class);
        SplitTimeAnalysisController controller = controller(null, mentalService, null, null, null);

        MentalResilienceAnalysis analysis = new MentalResilienceAnalysis(
                ResultListId.of(1L), EventId.of(1L), List.of(),
                new MriStatistics(5, 0, 0, 0, 0, 0, null, null));
        when(mentalService.analyzeMentalResilience(ResultListId.of(1L), List.of())).thenReturn(analysis);

        var result = controller.analyzeMentalResilience(1L, null);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void analyzeMentalResilience_shouldReturnOk_whenMistakesDetected() {
        MentalResilienceService mentalService = mock(MentalResilienceService.class);
        SplitTimeAnalysisController controller = controller(null, mentalService, null, null, null);

        MentalResilienceAnalysis analysis = new MentalResilienceAnalysis(
                ResultListId.of(2L), EventId.of(1L), List.of(),
                new MriStatistics(5, 2, 3, 1, 1, 0, 1.2, 1.1));
        when(mentalService.analyzeMentalResilience(ResultListId.of(2L), List.of(1L))).thenReturn(analysis);

        var result = controller.analyzeMentalResilience(2L, List.of(1L));

        assertThat(result.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void anomalyDetection_shouldReturnOk() {
        AnomalyDetectionService anomalyService = mock(AnomalyDetectionService.class);
        SplitTimeAnalysisController controller = controller(null, null, anomalyService, null, null);

        AnomalyAnalysis analysis = new AnomalyAnalysis(ResultListId.of(1L), EventId.of(1L), List.of());
        when(anomalyService.analyzeAnomaly(ResultListId.of(1L), List.of())).thenReturn(analysis);

        var result = controller.anomalyDetection(1L, null);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void hangingDetection_shouldReturnOk_whenNoHanging() {
        HangingDetectionService hangingService = mock(HangingDetectionService.class);
        SplitTimeAnalysisController controller = controller(null, null, null, hangingService, null);

        HangingAnalysis analysis = new HangingAnalysis(ResultListId.of(1L), EventId.of(1L), List.of(),
                new HangingStatistics(5, 0, 0, 0, 0, null, null));
        when(hangingService.analyzeHanging(ResultListId.of(1L), List.of())).thenReturn(analysis);

        var result = controller.hangingDetection(1L, null);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void hangingDetection_shouldReturnOk_whenHangingDetected() {
        HangingDetectionService hangingService = mock(HangingDetectionService.class);
        SplitTimeAnalysisController controller = controller(null, null, null, hangingService, null);

        HangingAnalysis analysis = new HangingAnalysis(ResultListId.of(3L), EventId.of(1L), List.of(),
                new HangingStatistics(5, 2, 4, 1, 1, 0.75, 0.80));
        when(hangingService.analyzeHanging(ResultListId.of(3L), List.of(1L))).thenReturn(analysis);

        var result = controller.hangingDetection(3L, List.of(1L));

        assertThat(result.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void getSplitTimeTable_byClass_shouldReturnTable() {
        SplitTimeTableService tableService = mock(SplitTimeTableService.class);
        SplitTimeAnalysisController controller = controller(null, null, null, null, tableService);

        SplitTimeTable table = new SplitTimeTable("CLASS", "H21",
                List.of("H21"), List.of("S", "31", "F"), List.of(),
                new SplitTimeTableMetadata(5, 3, 3, true, 360.0));
        when(tableService.generateByClass(ResultListId.of(1L), "H21")).thenReturn(table);

        var result = controller.getSplitTimeTable(1L, "class", "H21");

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        verify(tableService).generateByClass(ResultListId.of(1L), "H21");
    }

    @Test
    void getSplitTimeTable_byCourse_shouldReturnTable() {
        SplitTimeTableService tableService = mock(SplitTimeTableService.class);
        SplitTimeAnalysisController controller = controller(null, null, null, null, tableService);

        SplitTimeTable table = new SplitTimeTable("COURSE", "42",
                List.of("H21"), List.of("S", "31", "F"), List.of(),
                new SplitTimeTableMetadata(5, 3, 3, true, 360.0));
        when(tableService.generateByCourse(ResultListId.of(1L), 42L)).thenReturn(table);

        var result = controller.getSplitTimeTable(1L, "course", "42");

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        verify(tableService).generateByCourse(ResultListId.of(1L), 42L);
    }

    @Test
    void getSplitTimeTable_invalidGroupBy_shouldThrowIllegalArgument() {
        SplitTimeAnalysisController controller = controller(null, null, null, null, mock(SplitTimeTableService.class));

        assertThatThrownBy(() -> controller.getSplitTimeTable(1L, "invalid", "H21"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid groupBy");
    }

    @Test
    void getSplitTableOptions_shouldReturnClassesAndCourses() {
        SplitTimeTableService tableService = mock(SplitTimeTableService.class);
        SplitTimeAnalysisController controller = controller(null, null, null, null, tableService);

        when(tableService.getAvailableClasses(ResultListId.of(1L)))
                .thenReturn(List.of(new ClassGroupOption("H21", 5)));
        when(tableService.getAvailableCourses(ResultListId.of(1L)))
                .thenReturn(List.of(new CourseGroupOption(1L, "Bahn A", List.of("H21"), 5)));

        var result = controller.getSplitTableOptions(1L);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody().classes()).hasSize(1);
        assertThat(result.getBody().courses()).hasSize(1);
    }

    private SplitTimeAnalysisController controller(
            SplitTimeRankingService ranking,
            MentalResilienceService mental,
            AnomalyDetectionService anomaly,
            HangingDetectionService hanging,
            SplitTimeTableService table) {
        return new SplitTimeAnalysisController(
                ranking != null ? ranking : mock(SplitTimeRankingService.class),
                mental != null ? mental : mock(MentalResilienceService.class),
                anomaly != null ? anomaly : mock(AnomalyDetectionService.class),
                hanging != null ? hanging : mock(HangingDetectionService.class),
                table != null ? table : mock(SplitTimeTableService.class));
    }
}
