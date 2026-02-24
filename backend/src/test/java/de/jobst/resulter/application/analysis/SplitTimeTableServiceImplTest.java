package de.jobst.resulter.application.analysis;

import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.*;
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
