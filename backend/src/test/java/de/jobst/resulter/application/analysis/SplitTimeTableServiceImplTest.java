package de.jobst.resulter.application.analysis;

import de.jobst.resulter.application.port.ClassGroupOption;
import de.jobst.resulter.application.port.CourseGroupOption;
import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.application.port.SegmentPI;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.analysis.PerformanceIndex;
import de.jobst.resulter.domain.analysis.SplitTimeTable;
import de.jobst.resulter.domain.analysis.SplitTimeTableRow;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.jobst.resulter.application.analysis.SplitTimeAnalysisServiceImpl.START_CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SplitTimeTableServiceImplTest {

    @Test
    void generateByClass_shouldNotShiftSplitTimesWhenControlIsMissing() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl splitTimeAnalysisService = mock(SplitTimeAnalysisServiceImpl.class);
        PersonRepository personRepository = mock(PersonRepository.class);

        SplitTimeTableServiceImpl service = new SplitTimeTableServiceImpl(
                splitTimeListRepository,
                resultListRepository,
                splitTimeAnalysisService,
                personRepository
        );

        ResultListId resultListId = ResultListId.of(10L);
        String className = "H21";

        SplitTimeList referenceRunner = splitTimeList(1L, className, List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.empty()),
                SplitTime.of("32", 200.0, SplitTimeListId.empty()),
                SplitTime.of("33", 300.0, SplitTimeListId.empty())
        ));

        SplitTimeList runnerWithMissingControl = splitTimeList(2L, className, List.of(
                SplitTime.of("31", 110.0, SplitTimeListId.empty()),
                SplitTime.of("33", 330.0, SplitTimeListId.empty())
        ));

        ResultList resultList = new ResultList(
                resultListId,
                EventId.of(1L),
                RaceId.of(1L),
                null,
                null,
                null,
                List.of()
        );

        when(splitTimeListRepository.findByResultListId(resultListId))
                .thenReturn(List.of(referenceRunner, runnerWithMissingControl));
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(splitTimeAnalysisService.buildRuntimeMap(resultList)).thenReturn(Map.of());
        when(splitTimeAnalysisService.calculateReferenceTimesPerSegment(anyList(), anyMap())).thenReturn(Map.of());
        when(splitTimeAnalysisService.calculateSegmentTimes(any(SplitTimeList.class), anyMap())).thenReturn(List.of());
        when(splitTimeAnalysisService.calculateSegmentPIs(anyList(), anyMap(), anyString())).thenReturn(List.of());
        when(splitTimeAnalysisService.calculateNormalPI(anyList())).thenReturn(null);
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());

        SplitTimeTable table = service.generateByClass(resultListId, className);

        assertThat(table.controlCodes()).containsExactly(START_CODE, "31", "32", "33");

        SplitTimeTableRow affectedRunnerRow = table.rows().stream()
                .filter(row -> row.personId().equals(2L))
                .findFirst()
                .orElseThrow();

        assertThat(affectedRunnerRow.cells().get(2).controlCode()).isEqualTo("32");
        assertThat(affectedRunnerRow.cells().get(2).cumulativeTime()).isNull();
        assertThat(affectedRunnerRow.cells().get(2).segmentTime()).isNull();

        assertThat(affectedRunnerRow.cells().get(3).controlCode()).isEqualTo("33");
        assertThat(affectedRunnerRow.cells().get(3).cumulativeTime()).isEqualTo(330.0);
        assertThat(affectedRunnerRow.cells().get(3).segmentTime()).isEqualTo(220.0);
        assertThat(affectedRunnerRow.cells().get(3).segmentPosition()).isNull();
    }

    @Test
    void generateByClass_shouldIgnoreNonPositiveSplitTimes() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl splitTimeAnalysisService = mock(SplitTimeAnalysisServiceImpl.class);
        PersonRepository personRepository = mock(PersonRepository.class);

        SplitTimeTableServiceImpl service = new SplitTimeTableServiceImpl(
                splitTimeListRepository,
                resultListRepository,
                splitTimeAnalysisService,
                personRepository
        );

        ResultListId resultListId = ResultListId.of(10L);
        String className = "H19";

        SplitTimeList referenceRunner = splitTimeList(1L, className, List.of(
                SplitTime.of("111", 150.0, SplitTimeListId.empty()),
                SplitTime.of("110", 300.0, SplitTimeListId.empty()),
                SplitTime.of("112", 500.0, SplitTimeListId.empty())
        ));

        SplitTimeList runnerWithZeroTimes = splitTimeList(2L, className, List.of(
                SplitTime.of("111", 0.0, SplitTimeListId.empty()),
                SplitTime.of("110", 0.0, SplitTimeListId.empty()),
                SplitTime.of("112", 500.0, SplitTimeListId.empty())
        ));

        ResultList resultList = new ResultList(
                resultListId,
                EventId.of(1L),
                RaceId.of(1L),
                null,
                null,
                null,
                List.of()
        );

        when(splitTimeListRepository.findByResultListId(resultListId))
                .thenReturn(List.of(referenceRunner, runnerWithZeroTimes));
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(splitTimeAnalysisService.buildRuntimeMap(resultList)).thenReturn(Map.of());
        when(splitTimeAnalysisService.calculateReferenceTimesPerSegment(anyList(), anyMap())).thenReturn(Map.of());
        when(splitTimeAnalysisService.calculateSegmentTimes(any(SplitTimeList.class), anyMap())).thenReturn(List.of());
        when(splitTimeAnalysisService.calculateSegmentPIs(anyList(), anyMap(), anyString())).thenReturn(List.of());
        when(splitTimeAnalysisService.calculateNormalPI(anyList())).thenReturn(null);
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());

        SplitTimeTable table = service.generateByClass(resultListId, className);

        SplitTimeTableRow row = table.rows().stream()
                .filter(r -> r.personId().equals(2L))
                .findFirst()
                .orElseThrow();

        assertThat(table.controlCodes()).containsExactly(START_CODE, "111", "110", "112");
        assertThat(row.cells().get(1).cumulativeTime()).isNull();
        assertThat(row.cells().get(2).cumulativeTime()).isNull();
        assertThat(row.cells().get(3).cumulativeTime()).isEqualTo(500.0);
    }

    @Test
    void generateByClass_shouldNotAssignOverallPositionForIncompleteSplits() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl splitTimeAnalysisService = mock(SplitTimeAnalysisServiceImpl.class);
        PersonRepository personRepository = mock(PersonRepository.class);

        SplitTimeTableServiceImpl service = new SplitTimeTableServiceImpl(
                splitTimeListRepository,
                resultListRepository,
                splitTimeAnalysisService,
                personRepository
        );

        ResultListId resultListId = ResultListId.of(10L);
        String className = "D21";

        SplitTimeList completeRunner = splitTimeList(1L, className, List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.empty()),
                SplitTime.of("32", 200.0, SplitTimeListId.empty()),
                SplitTime.of("33", 300.0, SplitTimeListId.empty())
        ));

        SplitTimeList incompleteRunner = splitTimeList(2L, className, List.of(
                SplitTime.of("31", 110.0, SplitTimeListId.empty()),
                SplitTime.of("33", 290.0, SplitTimeListId.empty())
        ));

        ResultList resultList = new ResultList(
                resultListId,
                EventId.of(1L),
                RaceId.of(1L),
                null,
                null,
                null,
                List.of()
        );

        when(splitTimeListRepository.findByResultListId(resultListId))
                .thenReturn(List.of(completeRunner, incompleteRunner));
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        Map<RuntimeKey, Double> runtimeMap = Map.of(
                new RuntimeKey(1L, className, 1), 300.0,
                new RuntimeKey(2L, className, 1), 290.0
        );
        when(splitTimeAnalysisService.buildRuntimeMap(resultList)).thenReturn(runtimeMap);
        when(splitTimeAnalysisService.calculateReferenceTimesPerSegment(anyList(), anyMap())).thenReturn(Map.of());
        when(splitTimeAnalysisService.calculateSegmentTimes(any(SplitTimeList.class), anyMap())).thenReturn(List.of());
        when(splitTimeAnalysisService.calculateSegmentPIs(anyList(), anyMap(), anyString())).thenReturn(List.of());
        when(splitTimeAnalysisService.calculateNormalPI(anyList())).thenReturn(null);
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());

        SplitTimeTable table = service.generateByClass(resultListId, className);

        SplitTimeTableRow completeRow = table.rows().stream().filter(r -> r.personId().equals(1L)).findFirst().orElseThrow();
        SplitTimeTableRow incompleteRow = table.rows().stream().filter(r -> r.personId().equals(2L)).findFirst().orElseThrow();

        assertThat(completeRow.hasIncompleteSplits()).isFalse();
        assertThat(completeRow.position()).isEqualTo(1);
        assertThat(incompleteRow.hasIncompleteSplits()).isTrue();
        assertThat(incompleteRow.position()).isNull();
    }

    @Test
    void generateByClass_shouldMarkRunnerIncompleteWhenDuplicateControlOccurrenceIsMissing() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl splitTimeAnalysisService = mock(SplitTimeAnalysisServiceImpl.class);
        PersonRepository personRepository = mock(PersonRepository.class);

        SplitTimeTableServiceImpl service = new SplitTimeTableServiceImpl(
                splitTimeListRepository,
                resultListRepository,
                splitTimeAnalysisService,
                personRepository
        );

        ResultListId resultListId = ResultListId.of(10L);
        String className = "H19";

        // Course sequence (from reference runner): 31 -> 32 -> 31 -> 33
        SplitTimeList referenceRunner = splitTimeList(1L, className, List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.empty()),
                SplitTime.of("32", 200.0, SplitTimeListId.empty()),
                SplitTime.of("31", 300.0, SplitTimeListId.empty()),
                SplitTime.of("33", 400.0, SplitTimeListId.empty())
        ));

        // Missing the second occurrence of control 31
        SplitTimeList incompleteRunner = splitTimeList(2L, className, List.of(
                SplitTime.of("31", 110.0, SplitTimeListId.empty()),
                SplitTime.of("32", 220.0, SplitTimeListId.empty()),
                SplitTime.of("33", 430.0, SplitTimeListId.empty())
        ));

        ResultList resultList = new ResultList(
                resultListId,
                EventId.of(1L),
                RaceId.of(1L),
                null,
                null,
                null,
                List.of()
        );

        when(splitTimeListRepository.findByResultListId(resultListId))
                .thenReturn(List.of(referenceRunner, incompleteRunner));
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        Map<RuntimeKey, Double> runtimeMap = Map.of(
                new RuntimeKey(1L, className, 1), 400.0,
                new RuntimeKey(2L, className, 1), 430.0
        );
        when(splitTimeAnalysisService.buildRuntimeMap(resultList)).thenReturn(runtimeMap);
        when(splitTimeAnalysisService.calculateReferenceTimesPerSegment(anyList(), anyMap())).thenReturn(Map.of());
        when(splitTimeAnalysisService.calculateSegmentTimes(any(SplitTimeList.class), anyMap())).thenReturn(List.of());
        when(splitTimeAnalysisService.calculateSegmentPIs(anyList(), anyMap(), anyString())).thenReturn(List.of());
        when(splitTimeAnalysisService.calculateNormalPI(anyList())).thenReturn(null);
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());

        SplitTimeTable table = service.generateByClass(resultListId, className);
        SplitTimeTableRow incompleteRow = table.rows().stream().filter(r -> r.personId().equals(2L)).findFirst().orElseThrow();

        assertThat(incompleteRow.hasIncompleteSplits()).isTrue();
        assertThat(incompleteRow.position()).isNull();
    }

    @Test
    void generateByClass_marksNotCompetingRunner_withNullPosition() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl splitTimeAnalysisService = mock(SplitTimeAnalysisServiceImpl.class);
        PersonRepository personRepository = mock(PersonRepository.class);

        SplitTimeTableServiceImpl service = new SplitTimeTableServiceImpl(
                splitTimeListRepository, resultListRepository, splitTimeAnalysisService, personRepository);

        ResultListId resultListId = ResultListId.of(70L);
        String className = "H21";

        PersonRaceResult prr = PersonRaceResult.of(className, 1L, null, null, 300.0, 1L, (byte) 1, ResultStatus.NOT_COMPETING);
        PersonResult pr = PersonResult.of(ClassResultShortName.of(className), PersonId.of(1L), null, List.of(prr));
        ClassResult cr = ClassResult.of("Herren 21", className, Gender.M, List.of(pr), null);
        ResultList resultList = new ResultList(
                resultListId, EventId.of(1L), RaceId.of(1L), null, null, null, List.of(cr));

        SplitTimeList runner = splitTimeList(1L, className, List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.empty()),
                SplitTime.of("32", 200.0, SplitTimeListId.empty()),
                SplitTime.of("33", 300.0, SplitTimeListId.empty())
        ));

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner));
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        Map<RuntimeKey, Double> runtimeMap = Map.of(new RuntimeKey(1L, className, 1), 300.0);
        when(splitTimeAnalysisService.buildRuntimeMap(resultList)).thenReturn(runtimeMap);
        when(splitTimeAnalysisService.calculateReferenceTimesPerSegment(anyList(), anyMap())).thenReturn(Map.of());
        when(splitTimeAnalysisService.calculateSegmentTimes(any(SplitTimeList.class), anyMap())).thenReturn(List.of());
        when(splitTimeAnalysisService.calculateSegmentPIs(anyList(), anyMap(), anyString())).thenReturn(List.of());
        when(splitTimeAnalysisService.calculateNormalPI(anyList())).thenReturn(null);
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());

        SplitTimeTable table = service.generateByClass(resultListId, className);

        SplitTimeTableRow row = table.rows().getFirst();
        assertThat(row.notCompeting()).isTrue();
        assertThat(row.position()).isNull();
    }

    @Test
    void generateByClass_detectsErrors_andClassifiesSeverityLow() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl splitTimeAnalysisService = mock(SplitTimeAnalysisServiceImpl.class);
        PersonRepository personRepository = mock(PersonRepository.class);

        SplitTimeTableServiceImpl service = new SplitTimeTableServiceImpl(
                splitTimeListRepository, resultListRepository, splitTimeAnalysisService, personRepository);

        ResultListId resultListId = ResultListId.of(71L);
        String className = "H21";

        SplitTimeList runner = splitTimeList(1L, className, List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.empty()),
                SplitTime.of("32", 211.0, SplitTimeListId.empty())
        ));

        ResultList resultList = new ResultList(
                resultListId, EventId.of(1L), RaceId.of(1L), null, null, null, List.of());

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner));
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(splitTimeAnalysisService.buildRuntimeMap(resultList)).thenReturn(Map.of());
        when(splitTimeAnalysisService.calculateReferenceTimesPerSegment(anyList(), anyMap())).thenReturn(Map.of());

        // normalPI = 1.0; segment 31→32 PI = 1.12 → magnitude = 0.12 → LOW severity
        List<de.jobst.resulter.application.analysis.SegmentTime> segTimes = List.of(
                new de.jobst.resulter.application.analysis.SegmentTime(1, "31", "32", 112.0)
        );
        when(splitTimeAnalysisService.calculateSegmentTimes(any(SplitTimeList.class), anyMap()))
                .thenReturn(segTimes);
        when(splitTimeAnalysisService.calculateSegmentPIs(anyList(), anyMap(), anyString()))
                .thenReturn(List.of(new SegmentPI(1, "31", "32", 112.0, 100.0, new PerformanceIndex(1.12))));
        when(splitTimeAnalysisService.calculateNormalPI(anyList()))
                .thenReturn(new PerformanceIndex(1.0));
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());

        SplitTimeTable table = service.generateByClass(resultListId, className);

        SplitTimeTableRow row = table.rows().getFirst();
        // Cell at index 2 = control "32"
        assertThat(row.cells().get(2).isError()).isTrue();
    }

    @Test
    void generateByClass_detectsErrors_andClassifiesSeverityMedium() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl splitTimeAnalysisService = mock(SplitTimeAnalysisServiceImpl.class);
        PersonRepository personRepository = mock(PersonRepository.class);

        SplitTimeTableServiceImpl service = new SplitTimeTableServiceImpl(
                splitTimeListRepository, resultListRepository, splitTimeAnalysisService, personRepository);

        ResultListId resultListId = ResultListId.of(72L);
        String className = "D21";

        SplitTimeList runner = splitTimeList(1L, className, List.of(
                SplitTime.of("41", 100.0, SplitTimeListId.empty()),
                SplitTime.of("42", 220.0, SplitTimeListId.empty())
        ));

        ResultList resultList = new ResultList(
                resultListId, EventId.of(1L), RaceId.of(1L), null, null, null, List.of());

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner));
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(splitTimeAnalysisService.buildRuntimeMap(resultList)).thenReturn(Map.of());
        when(splitTimeAnalysisService.calculateReferenceTimesPerSegment(anyList(), anyMap())).thenReturn(Map.of());

        // normalPI = 1.0; segment 41→42 PI = 1.20 → magnitude = 0.20 → MEDIUM severity
        when(splitTimeAnalysisService.calculateSegmentTimes(any(SplitTimeList.class), anyMap()))
                .thenReturn(List.of(new de.jobst.resulter.application.analysis.SegmentTime(1, "41", "42", 120.0)));
        when(splitTimeAnalysisService.calculateSegmentPIs(anyList(), anyMap(), anyString()))
                .thenReturn(List.of(new SegmentPI(1, "41", "42", 120.0, 100.0, new PerformanceIndex(1.20))));
        when(splitTimeAnalysisService.calculateNormalPI(anyList()))
                .thenReturn(new PerformanceIndex(1.0));
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());

        SplitTimeTable table = service.generateByClass(resultListId, className);

        SplitTimeTableRow row = table.rows().getFirst();
        assertThat(row.cells().get(2).isError()).isTrue();
        assertThat(row.cells().get(2).errorMagnitude()).isGreaterThan(0.15);
    }

    @Test
    void generateByClass_detectsErrors_andClassifiesSeverityHigh() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl splitTimeAnalysisService = mock(SplitTimeAnalysisServiceImpl.class);
        PersonRepository personRepository = mock(PersonRepository.class);

        SplitTimeTableServiceImpl service = new SplitTimeTableServiceImpl(
                splitTimeListRepository, resultListRepository, splitTimeAnalysisService, personRepository);

        ResultListId resultListId = ResultListId.of(73L);
        String className = "H10";

        SplitTimeList runner = splitTimeList(1L, className, List.of(
                SplitTime.of("51", 100.0, SplitTimeListId.empty()),
                SplitTime.of("52", 240.0, SplitTimeListId.empty())
        ));

        ResultList resultList = new ResultList(
                resultListId, EventId.of(1L), RaceId.of(1L), null, null, null, List.of());

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner));
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(splitTimeAnalysisService.buildRuntimeMap(resultList)).thenReturn(Map.of());
        when(splitTimeAnalysisService.calculateReferenceTimesPerSegment(anyList(), anyMap())).thenReturn(Map.of());

        // normalPI = 1.0; PI = 1.40 → magnitude = 0.40 → HIGH
        when(splitTimeAnalysisService.calculateSegmentTimes(any(SplitTimeList.class), anyMap()))
                .thenReturn(List.of(new de.jobst.resulter.application.analysis.SegmentTime(1, "51", "52", 140.0)));
        when(splitTimeAnalysisService.calculateSegmentPIs(anyList(), anyMap(), anyString()))
                .thenReturn(List.of(new SegmentPI(1, "51", "52", 140.0, 100.0, new PerformanceIndex(1.40))));
        when(splitTimeAnalysisService.calculateNormalPI(anyList()))
                .thenReturn(new PerformanceIndex(1.0));
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());

        SplitTimeTable table = service.generateByClass(resultListId, className);

        assertThat(table.rows().getFirst().cells().get(2).isError()).isTrue();
    }

    @Test
    void generateByClass_detectsErrors_andClassifiesSeveritySevere() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl splitTimeAnalysisService = mock(SplitTimeAnalysisServiceImpl.class);
        PersonRepository personRepository = mock(PersonRepository.class);

        SplitTimeTableServiceImpl service = new SplitTimeTableServiceImpl(
                splitTimeListRepository, resultListRepository, splitTimeAnalysisService, personRepository);

        ResultListId resultListId = ResultListId.of(74L);
        String className = "H12";

        SplitTimeList runner = splitTimeList(1L, className, List.of(
                SplitTime.of("61", 100.0, SplitTimeListId.empty()),
                SplitTime.of("62", 260.0, SplitTimeListId.empty())
        ));

        ResultList resultList = new ResultList(
                resultListId, EventId.of(1L), RaceId.of(1L), null, null, null, List.of());

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner));
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(splitTimeAnalysisService.buildRuntimeMap(resultList)).thenReturn(Map.of());
        when(splitTimeAnalysisService.calculateReferenceTimesPerSegment(anyList(), anyMap())).thenReturn(Map.of());

        // normalPI = 1.0; PI = 1.60 → magnitude = 0.60 → SEVERE
        when(splitTimeAnalysisService.calculateSegmentTimes(any(SplitTimeList.class), anyMap()))
                .thenReturn(List.of(new de.jobst.resulter.application.analysis.SegmentTime(1, "61", "62", 160.0)));
        when(splitTimeAnalysisService.calculateSegmentPIs(anyList(), anyMap(), anyString()))
                .thenReturn(List.of(new SegmentPI(1, "61", "62", 160.0, 100.0, new PerformanceIndex(1.60))));
        when(splitTimeAnalysisService.calculateNormalPI(anyList()))
                .thenReturn(new PerformanceIndex(1.0));
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());

        SplitTimeTable table = service.generateByClass(resultListId, className);

        assertThat(table.rows().getFirst().cells().get(2).isError()).isTrue();
    }

    @Test
    void generateByClass_returnsEmptyTable_whenNoSplitsMatchClass() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl splitTimeAnalysisService = mock(SplitTimeAnalysisServiceImpl.class);
        PersonRepository personRepository = mock(PersonRepository.class);

        SplitTimeTableServiceImpl service = new SplitTimeTableServiceImpl(
                splitTimeListRepository, resultListRepository, splitTimeAnalysisService, personRepository);

        ResultListId resultListId = ResultListId.of(30L);

        when(splitTimeListRepository.findByResultListId(resultListId))
                .thenReturn(List.of(splitTimeList(1L, "D21", List.of())));

        SplitTimeTable table = service.generateByClass(resultListId, "H21");

        assertThat(table.groupByType()).isEqualTo("CLASS");
        assertThat(table.rows()).isEmpty();
        assertThat(table.controlCodes()).isEmpty();
    }

    @Test
    void getAvailableClasses_returnsGroupedAndSortedOptions() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl splitTimeAnalysisService = mock(SplitTimeAnalysisServiceImpl.class);
        PersonRepository personRepository = mock(PersonRepository.class);

        SplitTimeTableServiceImpl service = new SplitTimeTableServiceImpl(
                splitTimeListRepository, resultListRepository, splitTimeAnalysisService, personRepository);

        ResultListId resultListId = ResultListId.of(40L);
        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(
                splitTimeList(1L, "H21", List.of()),
                splitTimeList(2L, "H21", List.of()),
                splitTimeList(3L, "D21", List.of())
        ));

        List<ClassGroupOption> options = service.getAvailableClasses(resultListId);

        assertThat(options).hasSize(2);
        assertThat(options.get(0).className()).isEqualTo("D21");
        assertThat(options.get(0).runnerCount()).isEqualTo(1);
        assertThat(options.get(1).className()).isEqualTo("H21");
        assertThat(options.get(1).runnerCount()).isEqualTo(2);
    }

    @Test
    void getAvailableClasses_returnsEmpty_whenNoSplits() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl splitTimeAnalysisService = mock(SplitTimeAnalysisServiceImpl.class);
        PersonRepository personRepository = mock(PersonRepository.class);

        SplitTimeTableServiceImpl service = new SplitTimeTableServiceImpl(
                splitTimeListRepository, resultListRepository, splitTimeAnalysisService, personRepository);

        ResultListId resultListId = ResultListId.of(41L);
        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of());

        List<ClassGroupOption> options = service.getAvailableClasses(resultListId);

        assertThat(options).isEmpty();
    }

    @Test
    void generateByCourse_returnsEmptyTable_whenNoClassesForCourse() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl splitTimeAnalysisService = mock(SplitTimeAnalysisServiceImpl.class);
        PersonRepository personRepository = mock(PersonRepository.class);

        SplitTimeTableServiceImpl service = new SplitTimeTableServiceImpl(
                splitTimeListRepository, resultListRepository, splitTimeAnalysisService, personRepository);

        ResultListId resultListId = ResultListId.of(50L);
        ResultList resultList = new ResultList(
                resultListId, EventId.of(1L), RaceId.of(1L), null, null, null, List.of());

        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));

        SplitTimeTable table = service.generateByCourse(resultListId, 99L);

        assertThat(table.groupByType()).isEqualTo("COURSE");
        assertThat(table.rows()).isEmpty();
    }

    @Test
    void generateByCourse_returnsEmptyTable_whenClassesFoundButNoSplits() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl splitTimeAnalysisService = mock(SplitTimeAnalysisServiceImpl.class);
        PersonRepository personRepository = mock(PersonRepository.class);

        SplitTimeTableServiceImpl service = new SplitTimeTableServiceImpl(
                splitTimeListRepository, resultListRepository, splitTimeAnalysisService, personRepository);

        ResultListId resultListId = ResultListId.of(51L);
        ClassResult classResult = ClassResult.of("Herren 21", "H21", Gender.M, List.of(), CourseId.of(1L));
        ResultList resultList = new ResultList(
                resultListId, EventId.of(1L), RaceId.of(1L), null, null, null, List.of(classResult));

        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of());

        SplitTimeTable table = service.generateByCourse(resultListId, 1L);

        assertThat(table.groupByType()).isEqualTo("COURSE");
        assertThat(table.rows()).isEmpty();
    }

    @Test
    void generateByCourse_generatesTable_whenClassAndSplitsFound() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl splitTimeAnalysisService = mock(SplitTimeAnalysisServiceImpl.class);
        PersonRepository personRepository = mock(PersonRepository.class);

        SplitTimeTableServiceImpl service = new SplitTimeTableServiceImpl(
                splitTimeListRepository, resultListRepository, splitTimeAnalysisService, personRepository);

        ResultListId resultListId = ResultListId.of(52L);
        ClassResult classResult = ClassResult.of("Herren 21", "H21", Gender.M, List.of(), CourseId.of(2L));
        ResultList resultList = new ResultList(
                resultListId, EventId.of(1L), RaceId.of(1L), null, null, null, List.of(classResult));

        SplitTimeList runner = splitTimeList(1L, "H21", List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.empty()),
                SplitTime.of("32", 200.0, SplitTimeListId.empty())
        ));

        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner));
        when(splitTimeAnalysisService.buildRuntimeMap(resultList)).thenReturn(Map.of());
        when(splitTimeAnalysisService.calculateReferenceTimesPerSegment(anyList(), anyMap())).thenReturn(Map.of());
        when(splitTimeAnalysisService.calculateSegmentTimes(any(SplitTimeList.class), anyMap())).thenReturn(List.of());
        when(splitTimeAnalysisService.calculateSegmentPIs(anyList(), anyMap(), anyString())).thenReturn(List.of());
        when(splitTimeAnalysisService.calculateNormalPI(anyList())).thenReturn(null);
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());

        SplitTimeTable table = service.generateByCourse(resultListId, 2L);

        assertThat(table.groupByType()).isEqualTo("COURSE");
        assertThat(table.rows()).hasSize(1);
        assertThat(table.controlCodes()).containsExactly(START_CODE, "31", "32");
    }

    @Test
    void getAvailableCourses_returnsEmpty_whenResultListHasNullClassResults() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl splitTimeAnalysisService = mock(SplitTimeAnalysisServiceImpl.class);
        PersonRepository personRepository = mock(PersonRepository.class);

        SplitTimeTableServiceImpl service = new SplitTimeTableServiceImpl(
                splitTimeListRepository, resultListRepository, splitTimeAnalysisService, personRepository);

        ResultListId resultListId = ResultListId.of(60L);
        ResultList resultList = new ResultList(
                resultListId, EventId.of(1L), RaceId.of(1L), null, null, null, null);

        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of());

        List<CourseGroupOption> options = service.getAvailableCourses(resultListId);

        assertThat(options).isEmpty();
    }

    @Test
    void getAvailableCourses_returnsCourseOptions_whenClassResultsHaveCourseId() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);
        SplitTimeAnalysisServiceImpl splitTimeAnalysisService = mock(SplitTimeAnalysisServiceImpl.class);
        PersonRepository personRepository = mock(PersonRepository.class);

        SplitTimeTableServiceImpl service = new SplitTimeTableServiceImpl(
                splitTimeListRepository, resultListRepository, splitTimeAnalysisService, personRepository);

        ResultListId resultListId = ResultListId.of(61L);
        ClassResult cr1 = ClassResult.of("Herren 21", "H21", Gender.M, List.of(), CourseId.of(1L));
        ClassResult cr2 = ClassResult.of("Damen 21", "D21", Gender.F, List.of(), CourseId.of(1L));
        ResultList resultList = new ResultList(
                resultListId, EventId.of(1L), RaceId.of(1L), null, null, null, List.of(cr1, cr2));

        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList));
        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(
                splitTimeList(1L, "H21", List.of()),
                splitTimeList(2L, "D21", List.of())
        ));

        List<CourseGroupOption> options = service.getAvailableCourses(resultListId);

        assertThat(options).hasSize(1);
        assertThat(options.getFirst().courseId()).isEqualTo(1L);
        assertThat(options.getFirst().classNames()).containsExactlyInAnyOrder("H21", "D21");
        assertThat(options.getFirst().runnerCount()).isEqualTo(2);
    }

    private static SplitTimeList splitTimeList(Long personId, String className, List<SplitTime> splitTimes) {
        return new SplitTimeList(
                SplitTimeListId.of(personId),
                EventId.of(1L),
                ResultListId.of(10L),
                ClassResultShortName.of(className),
                PersonId.of(personId),
                RaceNumber.of((byte) 1),
                splitTimes
        );
    }
}
