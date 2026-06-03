package de.jobst.resulter.application.analysis;

import de.jobst.resulter.application.analysis.SegmentTime;
import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.application.port.SegmentPI;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.*;

import java.time.ZonedDateTime;
import de.jobst.resulter.domain.analysis.HangingAnalysis;
import de.jobst.resulter.domain.analysis.HangingClassification;
import de.jobst.resulter.domain.analysis.PerformanceIndex;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    // Vollständige Analyse – normalPI vorhanden, keine Hängerei
    // -------------------------------------------------------------------------

    @Test
    void analyzeHanging_returnsNoHanging_whenSegmentPIsAboveThreshold() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl analysisService = mock(SplitTimeAnalysisServiceImpl.class);
        HangingDetectionServiceImpl service = new HangingDetectionServiceImpl(stlRepo, rlRepo, analysisService);

        SplitTimeList stl1 = splitTimeListWithSplits("H21", 1L, List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.empty()),
                SplitTime.of("32", 190.0, SplitTimeListId.empty())
        ));
        SplitTimeList stl2 = splitTimeList("H21", 2L);
        SplitTimeList stl3 = splitTimeList("H21", 3L);

        ResultList resultList = emptyResultList();
        List<SegmentPI> segPIs = List.of(
                new SegmentPI(1, "31", "32", 90.0, 90.0, new PerformanceIndex(1.0))
        );

        when(stlRepo.findByResultListId(resultListId)).thenReturn(List.of(stl1, stl2, stl3));
        when(rlRepo.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(analysisService.buildRuntimeMap(any())).thenReturn(Map.of());
        when(analysisService.countRunnersPerClass(any())).thenReturn(Map.of("H21", 5));
        when(analysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());
        when(analysisService.calculateSegmentTimes(any(), any()))
                .thenReturn(List.of(new SegmentTime(1, "31", "32", 90.0)));
        when(analysisService.calculateSegmentPIs(any(), any(), any())).thenReturn(segPIs);
        when(analysisService.calculateNormalPI(any())).thenReturn(new PerformanceIndex(1.0));

        HangingAnalysis result = service.analyzeHanging(resultListId, List.of());

        assertThat(result.runnerProfiles()).hasSize(3);
        result.runnerProfiles().forEach(p ->
                assertThat(p.classification()).isEqualTo(HangingClassification.NO_HANGING));
        assertThat(result.statistics().totalRunners()).isEqualTo(3);
        assertThat(result.statistics().runnersWithHanging()).isEqualTo(0);
    }

    @Test
    void analyzeHanging_returnsInsufficientData_whenOnlyStartFinalSegments() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl analysisService = mock(SplitTimeAnalysisServiceImpl.class);
        HangingDetectionServiceImpl service = new HangingDetectionServiceImpl(stlRepo, rlRepo, analysisService);

        SplitTimeList stl = splitTimeList("H21", 1L);
        ResultList resultList = emptyResultList();

        // Nur Start→31 und 31→Ziel – beide werden in detectHangingPairs übersprungen
        List<SegmentPI> segPIs = List.of(
                new SegmentPI(1, "S", "31", 90.0, 90.0, new PerformanceIndex(1.0)),
                new SegmentPI(2, "31", "F", 60.0, 60.0, new PerformanceIndex(1.0))
        );

        when(stlRepo.findByResultListId(resultListId)).thenReturn(List.of(stl));
        when(rlRepo.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(analysisService.buildRuntimeMap(any())).thenReturn(Map.of());
        when(analysisService.countRunnersPerClass(any())).thenReturn(Map.of("H21", 5));
        when(analysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());
        when(analysisService.calculateSegmentTimes(any(), any()))
                .thenReturn(List.of(new SegmentTime(1, "S", "31", 90.0)));
        when(analysisService.calculateSegmentPIs(any(), any(), any())).thenReturn(segPIs);
        when(analysisService.calculateNormalPI(any())).thenReturn(new PerformanceIndex(1.0));

        HangingAnalysis result = service.analyzeHanging(resultListId, List.of());

        assertThat(result.runnerProfiles()).hasSize(1);
        assertThat(result.runnerProfiles().getFirst().classification())
                .isEqualTo(HangingClassification.INSUFFICIENT_DATA);
    }

    @Test
    void analyzeHanging_returnsEmptyProfile_whenNormalPINull() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl analysisService = mock(SplitTimeAnalysisServiceImpl.class);
        HangingDetectionServiceImpl service = new HangingDetectionServiceImpl(stlRepo, rlRepo, analysisService);

        SplitTimeList stl = splitTimeList("H21", 1L);
        ResultList resultList = emptyResultList();

        when(stlRepo.findByResultListId(resultListId)).thenReturn(List.of(stl));
        when(rlRepo.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(analysisService.buildRuntimeMap(any())).thenReturn(Map.of());
        when(analysisService.countRunnersPerClass(any())).thenReturn(Map.of("H21", 5));
        when(analysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());
        when(analysisService.calculateSegmentTimes(any(), any())).thenReturn(List.of());
        when(analysisService.calculateSegmentPIs(any(), any(), any())).thenReturn(
                List.of(new SegmentPI(1, "31", "32", 90.0, 90.0, new PerformanceIndex(1.0))));
        when(analysisService.calculateNormalPI(any())).thenReturn(null);

        HangingAnalysis result = service.analyzeHanging(resultListId, List.of());

        // Profile nicht inkludiert wenn normalPI null
        assertThat(result.runnerProfiles()).isEmpty();
        assertThat(result.statistics().totalRunners()).isEqualTo(1);
    }

    @Test
    void analyzeHanging_returnsHighHanging_whenBusDriverDetected() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl analysisService = mock(SplitTimeAnalysisServiceImpl.class);
        HangingDetectionServiceImpl service = new HangingDetectionServiceImpl(stlRepo, rlRepo, analysisService);

        // Passenger kommt 20s nach Driver an "32" an → innerhalb 30s Fenster
        SplitTimeList passenger = splitTimeListWithSplits("H21", 1L, List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.empty()),
                SplitTime.of("32", 130.0, SplitTimeListId.empty())
        ));
        SplitTimeList driver = splitTimeListWithSplits("H21", 2L, List.of(
                SplitTime.of("31", 80.0, SplitTimeListId.empty()),
                SplitTime.of("32", 110.0, SplitTimeListId.empty())
        ));

        ResultList resultList = emptyResultList();

        // Passenger: PI=0.5, HI=0.5/1.0=0.5 ≤ 0.85 → hängt
        List<SegmentPI> passengerSegPIs = List.of(
                new SegmentPI(1, "31", "32", 30.0, 90.0, new PerformanceIndex(0.5)));
        // Driver: PI=0.3 (schneller als Passenger)
        List<SegmentPI> driverSegPIs = List.of(
                new SegmentPI(1, "31", "32", 20.0, 90.0, new PerformanceIndex(0.3)));

        when(stlRepo.findByResultListId(resultListId)).thenReturn(List.of(passenger, driver));
        when(rlRepo.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(analysisService.buildRuntimeMap(any())).thenReturn(Map.of());
        when(analysisService.countRunnersPerClass(any())).thenReturn(Map.of("H21", 5));
        when(analysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());
        when(analysisService.calculateSegmentTimes(eq(passenger), any()))
                .thenReturn(List.of(new SegmentTime(1, "31", "32", 30.0)));
        when(analysisService.calculateSegmentTimes(eq(driver), any()))
                .thenReturn(List.of(new SegmentTime(1, "31", "32", 20.0)));
        when(analysisService.calculateSegmentPIs(any(), any(), eq("H21")))
                .thenReturn(passengerSegPIs) // preCompute passenger
                .thenReturn(driverSegPIs)    // preCompute driver
                .thenReturn(passengerSegPIs) // analyzeRunner passenger
                .thenReturn(driverSegPIs);   // analyzeRunner driver
        when(analysisService.calculateNormalPI(eq(passengerSegPIs))).thenReturn(new PerformanceIndex(1.0));
        when(analysisService.calculateNormalPI(eq(driverSegPIs))).thenReturn(new PerformanceIndex(1.0));
        when(analysisService.calculateNormalPI(eq(List.of()))).thenReturn(null);

        HangingAnalysis result = service.analyzeHanging(resultListId, List.of());

        assertThat(result.runnerProfiles()).hasSize(2);
        var passengerProfile = result.runnerProfiles().stream()
                .filter(p -> p.personId().value().equals(1L))
                .findFirst().orElseThrow();
        assertThat(passengerProfile.classification()).isEqualTo(HangingClassification.HIGH_HANGING);
        assertThat(passengerProfile.hangingPairs()).hasSize(1);
        assertThat(passengerProfile.hangingPairs().getFirst().busDriverId().value()).isEqualTo(2L);
        assertThat(result.statistics().runnersWithHanging()).isEqualTo(1);
        assertThat(result.statistics().highHangingRunners()).isEqualTo(1);
    }

    @Test
    void analyzeHanging_returnsModerateHanging_whenOneSegmentOutOfFive() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl analysisService = mock(SplitTimeAnalysisServiceImpl.class);
        HangingDetectionServiceImpl service = new HangingDetectionServiceImpl(stlRepo, rlRepo, analysisService);

        // Passenger arrives at "32" at 170s, Driver at 150s → 20s within 30s window
        SplitTimeList passenger = splitTimeListWithSplits("H21", 1L, List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.empty()),
                SplitTime.of("32", 170.0, SplitTimeListId.empty()),
                SplitTime.of("33", 250.0, SplitTimeListId.empty()),
                SplitTime.of("34", 320.0, SplitTimeListId.empty()),
                SplitTime.of("35", 400.0, SplitTimeListId.empty()),
                SplitTime.of("36", 480.0, SplitTimeListId.empty())
        ));
        SplitTimeList driver = splitTimeListWithSplits("H21", 2L, List.of(
                SplitTime.of("31", 80.0, SplitTimeListId.empty()),
                SplitTime.of("32", 150.0, SplitTimeListId.empty()),
                SplitTime.of("33", 220.0, SplitTimeListId.empty()),
                SplitTime.of("34", 290.0, SplitTimeListId.empty()),
                SplitTime.of("35", 360.0, SplitTimeListId.empty()),
                SplitTime.of("36", 430.0, SplitTimeListId.empty())
        ));

        ResultList resultList = emptyResultList();

        // 7 segments: S→31, 31→32(hanging), 32→33, 33→34, 34→35, 35→36, 36→F
        // totalNonMistakeSegments = 5 (inner segments only), hangingCount=1 → 20% < 30% → MODERATE
        List<SegmentPI> passengerSegPIs = List.of(
                new SegmentPI(0, "S",  "31", 100.0, 90.0, new PerformanceIndex(1.0)),
                new SegmentPI(1, "31", "32",  70.0, 90.0, new PerformanceIndex(0.5)),
                new SegmentPI(2, "32", "33",  80.0, 90.0, new PerformanceIndex(1.0)),
                new SegmentPI(3, "33", "34",  70.0, 90.0, new PerformanceIndex(1.0)),
                new SegmentPI(4, "34", "35",  80.0, 90.0, new PerformanceIndex(1.0)),
                new SegmentPI(5, "35", "36",  80.0, 90.0, new PerformanceIndex(1.0)),
                new SegmentPI(6, "36", "F",   80.0, 90.0, new PerformanceIndex(1.0))
        );
        List<SegmentPI> driverSegPIs = List.of(
                new SegmentPI(0, "S",  "31",  80.0, 90.0, new PerformanceIndex(1.0)),
                new SegmentPI(1, "31", "32",  70.0, 90.0, new PerformanceIndex(0.3)),
                new SegmentPI(2, "32", "33",  70.0, 90.0, new PerformanceIndex(1.0)),
                new SegmentPI(3, "33", "34",  70.0, 90.0, new PerformanceIndex(1.0)),
                new SegmentPI(4, "34", "35",  70.0, 90.0, new PerformanceIndex(1.0)),
                new SegmentPI(5, "35", "36",  70.0, 90.0, new PerformanceIndex(1.0)),
                new SegmentPI(6, "36", "F",   70.0, 90.0, new PerformanceIndex(1.0))
        );

        when(stlRepo.findByResultListId(resultListId)).thenReturn(List.of(passenger, driver));
        when(rlRepo.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(analysisService.buildRuntimeMap(resultList)).thenReturn(Map.of());
        when(analysisService.countRunnersPerClass(any())).thenReturn(Map.of("H21", 5));
        when(analysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());
        when(analysisService.calculateSegmentTimes(eq(passenger), any()))
                .thenReturn(List.of(new SegmentTime(1, "31", "32", 70.0)));
        when(analysisService.calculateSegmentTimes(eq(driver), any()))
                .thenReturn(List.of(new SegmentTime(1, "31", "32", 70.0)));
        when(analysisService.calculateSegmentPIs(any(), any(), eq("H21")))
                .thenReturn(passengerSegPIs)
                .thenReturn(driverSegPIs)
                .thenReturn(passengerSegPIs)
                .thenReturn(driverSegPIs);
        when(analysisService.calculateNormalPI(passengerSegPIs)).thenReturn(new PerformanceIndex(1.0));
        when(analysisService.calculateNormalPI(driverSegPIs)).thenReturn(new PerformanceIndex(1.0));

        HangingAnalysis result = service.analyzeHanging(resultListId, List.of());

        assertThat(result.runnerProfiles()).hasSize(2);
        var passengerProfile = result.runnerProfiles().stream()
                .filter(p -> p.personId().value().equals(1L))
                .findFirst().orElseThrow();
        assertThat(passengerProfile.classification()).isEqualTo(HangingClassification.MODERATE_HANGING);
        assertThat(result.statistics().moderateHangingRunners()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // buildStartTimeMap mit echten ClassResults
    // -------------------------------------------------------------------------

    @Test
    void analyzeHanging_extractsStartTime_fromResultListClassResults() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl analysisService = mock(SplitTimeAnalysisServiceImpl.class);
        HangingDetectionServiceImpl service = new HangingDetectionServiceImpl(stlRepo, rlRepo, analysisService);

        SplitTimeList stl1 = splitTimeListWithSplits("H21", 1L, List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.empty()),
                SplitTime.of("32", 190.0, SplitTimeListId.empty())
        ));
        SplitTimeList stl2 = splitTimeListWithSplits("H21", 2L, List.of());
        SplitTimeList stl3 = splitTimeListWithSplits("H21", 3L, List.of());

        // ResultList mit echten ClassResults → deckt buildStartTimeMap-Schleifenkörper ab
        ZonedDateTime startZdt = ZonedDateTime.of(2024, 10, 5, 10, 0, 0, 0, java.time.ZoneOffset.UTC);
        PersonRaceResult prr = PersonRaceResult.of("H21", 1L, startZdt, null, 100.0, 1L, (byte) 1, ResultStatus.OK);
        PersonResult pr = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of(prr));
        ClassResult cr = ClassResult.of("H21", "H21", Gender.M, List.of(pr), null);
        ResultList rlWithClassData = new ResultList(resultListId, EventId.of(1L), RaceId.of(1L),
                null, null, null, List.of(cr));

        List<SegmentPI> segPIs = List.of(
                new SegmentPI(1, "31", "32", 90.0, 90.0, new PerformanceIndex(1.0))
        );

        when(stlRepo.findByResultListId(resultListId)).thenReturn(List.of(stl1, stl2, stl3));
        when(rlRepo.findById(resultListId)).thenReturn(Optional.of(rlWithClassData));
        when(analysisService.buildRuntimeMap(any())).thenReturn(Map.of());
        when(analysisService.countRunnersPerClass(any())).thenReturn(Map.of("H21", 5));
        when(analysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());
        when(analysisService.calculateSegmentTimes(any(), any()))
                .thenReturn(List.of(new SegmentTime(1, "31", "32", 90.0)));
        when(analysisService.calculateSegmentPIs(any(), any(), any())).thenReturn(segPIs);
        when(analysisService.calculateNormalPI(any())).thenReturn(new PerformanceIndex(1.0));

        HangingAnalysis result = service.analyzeHanging(resultListId, List.of());

        assertThat(result.runnerProfiles()).hasSize(3);
        var profile1 = result.runnerProfiles().stream()
                .filter(p -> p.personId().value().equals(1L))
                .findFirst().orElseThrow();
        // Runner 1 hat StartZeit aus ClassResults (EpochSeconds als Double)
        assertThat(profile1.startTime().value()).isNotNull();
        // Runner 2 hat keine StartZeit in den ClassResults
        var profile2 = result.runnerProfiles().stream()
                .filter(p -> p.personId().value().equals(2L))
                .findFirst().orElseThrow();
        assertThat(profile2.startTime().value()).isNull();
    }

    // -------------------------------------------------------------------------
    // Iterativer Konvergenz-Loop
    // -------------------------------------------------------------------------

    @Test
    void analyzeHanging_convergesInIterativeLoop_whenCleanSegmentsExist() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl analysisService = mock(SplitTimeAnalysisServiceImpl.class);
        HangingDetectionServiceImpl service = new HangingDetectionServiceImpl(stlRepo, rlRepo, analysisService);

        // Passenger: 31@100s, 32@170s, 33@240s
        SplitTimeList passenger = splitTimeListWithSplits("H21", 1L, List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.empty()),
                SplitTime.of("32", 170.0, SplitTimeListId.empty()),
                SplitTime.of("33", 240.0, SplitTimeListId.empty())
        ));
        // Driver: 32@150s (20s vor Passenger → innerhalb 30s-Fenster)
        SplitTimeList driver = splitTimeListWithSplits("H21", 2L, List.of(
                SplitTime.of("31",  80.0, SplitTimeListId.empty()),
                SplitTime.of("32", 150.0, SplitTimeListId.empty()),
                SplitTime.of("33", 220.0, SplitTimeListId.empty())
        ));
        ResultList resultList = emptyResultList();

        // Passenger: Segment 31→32 hängt (PI=0.5), Segment 32→33 normal (PI=1.0)
        List<SegmentPI> passengerSegPIs = List.of(
                new SegmentPI(1, "31", "32", 70.0, 90.0, new PerformanceIndex(0.5)),
                new SegmentPI(2, "32", "33", 70.0, 90.0, new PerformanceIndex(1.0))
        );
        List<SegmentPI> driverSegPIs = List.of(
                new SegmentPI(1, "31", "32", 70.0, 90.0, new PerformanceIndex(0.3)),
                new SegmentPI(2, "32", "33", 70.0, 90.0, new PerformanceIndex(1.0))
        );

        when(stlRepo.findByResultListId(resultListId)).thenReturn(List.of(passenger, driver));
        when(rlRepo.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(analysisService.buildRuntimeMap(any())).thenReturn(Map.of());
        when(analysisService.countRunnersPerClass(any())).thenReturn(Map.of("H21", 5));
        when(analysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());
        when(analysisService.calculateSegmentTimes(eq(passenger), any()))
                .thenReturn(List.of(new SegmentTime(1, "31", "32", 70.0)));
        when(analysisService.calculateSegmentTimes(eq(driver), any()))
                .thenReturn(List.of(new SegmentTime(1, "31", "32", 70.0)));
        when(analysisService.calculateSegmentPIs(any(), any(), any()))
                .thenReturn(passengerSegPIs)  // preCompute Passenger
                .thenReturn(driverSegPIs)     // preCompute Driver
                .thenReturn(passengerSegPIs)  // analyzeRunner Passenger
                .thenReturn(driverSegPIs);    // analyzeRunner Driver

        // 1. Aufruf: initiale normalPI für Passenger (PI=1.0)
        // 2. Aufruf: Konvergenz-Check mit cleanSegments=[32→33] → PI=1.005, diff=0.005 < 0.01 → Konvergenz
        // 3. Aufruf: initiale normalPI für Driver (PI=1.0)
        when(analysisService.calculateNormalPI(any()))
                .thenReturn(new PerformanceIndex(1.0))
                .thenReturn(new PerformanceIndex(1.005))
                .thenReturn(new PerformanceIndex(1.0));

        HangingAnalysis result = service.analyzeHanging(resultListId, List.of());

        assertThat(result.runnerProfiles()).hasSize(2);
        var passengerProfile = result.runnerProfiles().stream()
                .filter(p -> p.personId().value().equals(1L))
                .findFirst().orElseThrow();
        assertThat(passengerProfile.classification()).isEqualTo(HangingClassification.HIGH_HANGING);
        assertThat(passengerProfile.hangingPairs()).hasSize(1);
        assertThat(result.statistics().runnersWithHanging()).isEqualTo(1);
        // medianHI ist bei 1 Hänge-Paar vorhanden (ungerade Liste → Median = erster Wert)
        assertThat(result.statistics().medianHangingIndex()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // Gerader Median (2 Hänge-Paare aus 2 Läufern)
    // -------------------------------------------------------------------------

    @Test
    void analyzeHanging_calculatesEvenMedian_whenTwoRunnersHaveHangingPairs() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl analysisService = mock(SplitTimeAnalysisServiceImpl.class);
        HangingDetectionServiceImpl service = new HangingDetectionServiceImpl(stlRepo, rlRepo, analysisService);

        // p1@32=130s, p2@32=140s, driver@32=110s
        SplitTimeList p1 = splitTimeListWithSplits("H21", 1L, List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.empty()),
                SplitTime.of("32", 130.0, SplitTimeListId.empty())
        ));
        SplitTimeList p2 = splitTimeListWithSplits("H21", 2L, List.of(
                SplitTime.of("31", 110.0, SplitTimeListId.empty()),
                SplitTime.of("32", 140.0, SplitTimeListId.empty())
        ));
        SplitTimeList driver = splitTimeListWithSplits("H21", 3L, List.of(
                SplitTime.of("31",  80.0, SplitTimeListId.empty()),
                SplitTime.of("32", 110.0, SplitTimeListId.empty())
        ));
        ResultList resultList = emptyResultList();

        List<SegmentPI> p1SegPIs = List.of(
                new SegmentPI(1, "31", "32", 30.0, 90.0, new PerformanceIndex(0.5)));
        List<SegmentPI> p2SegPIs = List.of(
                new SegmentPI(1, "31", "32", 30.0, 90.0, new PerformanceIndex(0.5)));
        List<SegmentPI> driverSegPIs = List.of(
                new SegmentPI(1, "31", "32", 30.0, 90.0, new PerformanceIndex(0.3)));

        when(stlRepo.findByResultListId(resultListId)).thenReturn(List.of(p1, p2, driver));
        when(rlRepo.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(analysisService.buildRuntimeMap(any())).thenReturn(Map.of());
        when(analysisService.countRunnersPerClass(any())).thenReturn(Map.of("H21", 5));
        when(analysisService.calculateReferenceTimesPerSegment(any(), any())).thenReturn(Map.of());
        when(analysisService.calculateSegmentTimes(any(), any()))
                .thenReturn(List.of(new SegmentTime(1, "31", "32", 30.0)));
        when(analysisService.calculateSegmentPIs(any(), any(), any()))
                .thenReturn(p1SegPIs)      // preCompute p1
                .thenReturn(p2SegPIs)      // preCompute p2
                .thenReturn(driverSegPIs)  // preCompute driver
                .thenReturn(p1SegPIs)      // analyzeRunner p1
                .thenReturn(p2SegPIs)      // analyzeRunner p2
                .thenReturn(driverSegPIs); // analyzeRunner driver

        // Konvergenz-Loop für p1/p2: cleanSegments=[] → null → break
        when(analysisService.calculateNormalPI(any())).thenReturn(new PerformanceIndex(1.0));
        when(analysisService.calculateNormalPI(eq(List.of()))).thenReturn(null);

        HangingAnalysis result = service.analyzeHanging(resultListId, List.of());

        assertThat(result.runnerProfiles()).hasSize(3);
        assertThat(result.statistics().runnersWithHanging()).isEqualTo(2);
        // allHIs = [0.5, 0.5] → Größe 2 (gerade) → Median = (0.5+0.5)/2 = 0.5
        assertThat(result.statistics().medianHangingIndex()).isEqualTo(0.5);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private SplitTimeList splitTimeList(String className, long personId) {
        return splitTimeListWithSplits(className, personId, List.of());
    }

    private SplitTimeList splitTimeListWithSplits(String className, long personId, List<SplitTime> splitTimes) {
        return new SplitTimeList(
                SplitTimeListId.empty(),
                EventId.of(1L),
                resultListId,
                ClassResultShortName.of(className),
                PersonId.of(personId),
                RaceNumber.of((byte) 1),
                splitTimes
        );
    }

    private ResultList emptyResultList() {
        return new ResultList(resultListId, EventId.of(1L), RaceId.of(1L), null, null, null, List.of());
    }
}
