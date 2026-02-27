package de.jobst.resulter.application.analysis;

import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.ClassResultShortName;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.RaceId;
import de.jobst.resulter.domain.RaceNumber;
import de.jobst.resulter.domain.ResultList;
import de.jobst.resulter.domain.ResultListId;
import de.jobst.resulter.domain.SplitTime;
import de.jobst.resulter.domain.SplitTimeList;
import de.jobst.resulter.domain.SplitTimeListId;
import de.jobst.resulter.domain.analysis.ControlSequenceSegment;
import de.jobst.resulter.domain.analysis.SplitTimeAnalysis;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SplitTimeRankingServiceImplTest {

    @Test
    void analyzeSplitTimesRanking_shouldSortSequencesByControlCountThenRunnerCount() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository,
                personRepository,
                resultListRepository
        );

        ResultListId resultListId = ResultListId.of(10L);
        SplitTimeList runner1 = splitTimeList(1L, "H21", List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.of(1L)),
                SplitTime.of("32", 190.0, SplitTimeListId.of(1L)),
                SplitTime.of("33", 300.0, SplitTimeListId.of(1L))
        ));
        SplitTimeList runner2 = splitTimeList(2L, "D21", List.of(
                SplitTime.of("31", 105.0, SplitTimeListId.of(2L)),
                SplitTime.of("32", 198.0, SplitTimeListId.of(2L)),
                SplitTime.of("40", 315.0, SplitTimeListId.of(2L))
        ));
        SplitTimeList runner3 = splitTimeList(3L, "D21", List.of(
                SplitTime.of("31", 108.0, SplitTimeListId.of(3L)),
                SplitTime.of("32", 205.0, SplitTimeListId.of(3L))
        ));

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner1, runner2, runner3));
        when(personRepository.findAllById(org.mockito.ArgumentMatchers.anySet())).thenReturn(Map.of());
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList(resultListId)));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                resultListId,
                false,
                List.of(),
                false,
                true,
                2
        );

        assertThat(analyses).hasSize(1);
        assertThat(analyses.getFirst().sequenceSegments()).isNotEmpty();
        assertThat(analyses.getFirst().sequenceSegments().stream()
                .map(ControlSequenceSegment::controls)
                .map(list -> list.stream().map(c -> c.value()).toList()))
                .containsExactly(
                        List.of("S", "31", "32"),
                        List.of("S", "31"),
                        List.of("31", "32")
                );
    }

    @Test
    void analyzeSplitTimesRanking_shouldExcludeSequenceSegmentsWithSingleRunner() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository,
                personRepository,
                resultListRepository
        );

        ResultListId resultListId = ResultListId.of(11L);
        SplitTimeList runner1 = splitTimeList(1L, "H21", List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.of(1L)),
                SplitTime.of("32", 190.0, SplitTimeListId.of(1L))
        ));

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner1));
        when(personRepository.findAllById(org.mockito.ArgumentMatchers.anySet())).thenReturn(Map.of());
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList(resultListId)));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                resultListId,
                false,
                List.of(),
                false,
                true,
                2
        );

        assertThat(analyses).hasSize(1);
        assertThat(analyses.getFirst().sequenceSegments()).isEmpty();
    }

    @Test
    void analyzeSplitTimesRanking_shouldRemoveShorterCoveredSequencesWithSameRunnerCount() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository,
                personRepository,
                resultListRepository
        );

        ResultListId resultListId = ResultListId.of(13L);
        SplitTimeList runner1 = splitTimeList(1L, "H21", List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.of(1L)),
                SplitTime.of("32", 190.0, SplitTimeListId.of(1L)),
                SplitTime.of("33", 300.0, SplitTimeListId.of(1L))
        ));
        SplitTimeList runner2 = splitTimeList(2L, "D21", List.of(
                SplitTime.of("30", 70.0, SplitTimeListId.of(2L)),
                SplitTime.of("31", 150.0, SplitTimeListId.of(2L)),
                SplitTime.of("32", 240.0, SplitTimeListId.of(2L)),
                SplitTime.of("33", 360.0, SplitTimeListId.of(2L))
        ));

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner1, runner2));
        when(personRepository.findAllById(org.mockito.ArgumentMatchers.anySet())).thenReturn(Map.of());
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList(resultListId)));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                resultListId,
                false,
                List.of(),
                false,
                true,
                2
        );

        assertThat(analyses).hasSize(1);
        assertThat(analyses.getFirst().sequenceSegments().stream()
                .map(ControlSequenceSegment::controls)
                .map(list -> list.stream().map(c -> c.value()).toList()))
                .containsExactly(List.of("31", "32", "33"));
    }

    @Test
    void analyzeSplitTimesRanking_shouldRemoveSequencesContainingOnlySingleCourse() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository,
                personRepository,
                resultListRepository
        );

        ResultListId resultListId = ResultListId.of(14L);
        SplitTimeList runner1 = splitTimeList(1L, "H21", List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.of(1L)),
                SplitTime.of("32", 190.0, SplitTimeListId.of(1L)),
                SplitTime.of("33", 300.0, SplitTimeListId.of(1L))
        ));
        SplitTimeList runner2 = splitTimeList(2L, "H21", List.of(
                SplitTime.of("31", 102.0, SplitTimeListId.of(2L)),
                SplitTime.of("32", 194.0, SplitTimeListId.of(2L)),
                SplitTime.of("33", 306.0, SplitTimeListId.of(2L))
        ));
        SplitTimeList runner3 = splitTimeList(3L, "D21", List.of(
                SplitTime.of("45", 120.0, SplitTimeListId.of(3L)),
                SplitTime.of("46", 240.0, SplitTimeListId.of(3L))
        ));

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner1, runner2, runner3));
        when(personRepository.findAllById(org.mockito.ArgumentMatchers.anySet())).thenReturn(Map.of());
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList(resultListId)));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                resultListId,
                false,
                List.of(),
                false,
                true,
                2
        );

        assertThat(analyses).hasSize(1);
        assertThat(analyses.getFirst().sequenceSegments()).isEmpty();
    }

    @Test
    void analyzeSplitTimesRanking_shouldReturnNoSequenceSegmentsWhenDisabled() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository,
                personRepository,
                resultListRepository
        );

        ResultListId resultListId = ResultListId.of(12L);
        SplitTimeList runner1 = splitTimeList(1L, "H21", List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.of(1L)),
                SplitTime.of("32", 190.0, SplitTimeListId.of(1L))
        ));
        SplitTimeList runner2 = splitTimeList(2L, "H21", List.of(
                SplitTime.of("31", 102.0, SplitTimeListId.of(2L)),
                SplitTime.of("32", 193.0, SplitTimeListId.of(2L))
        ));

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner1, runner2));
        when(personRepository.findAllById(org.mockito.ArgumentMatchers.anySet())).thenReturn(Map.of());
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList(resultListId)));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                resultListId,
                false,
                List.of(),
                false,
                false,
                2
        );

        assertThat(analyses).hasSize(1);
        assertThat(analyses.getFirst().sequenceSegments()).isEmpty();
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

    private static ResultList resultList(ResultListId id) {
        return new ResultList(
                id,
                EventId.of(1L),
                RaceId.of(1L),
                null,
                null,
                null,
                List.of()
        );
    }

}
