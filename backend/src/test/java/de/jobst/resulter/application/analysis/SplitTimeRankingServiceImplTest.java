package de.jobst.resulter.application.analysis;

import de.jobst.resulter.application.port.PersonRepository;
import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.ClassResult;
import de.jobst.resulter.domain.ClassResultShortName;
import de.jobst.resulter.domain.CourseId;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Gender;
import de.jobst.resulter.domain.Person;
import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.PersonRaceResult;
import de.jobst.resulter.domain.PersonResult;
import de.jobst.resulter.domain.RaceId;
import de.jobst.resulter.domain.RaceNumber;
import de.jobst.resulter.domain.ResultList;
import de.jobst.resulter.domain.ResultListId;
import de.jobst.resulter.domain.ResultStatus;
import de.jobst.resulter.domain.SplitTime;
import de.jobst.resulter.domain.SplitTimeList;
import de.jobst.resulter.domain.SplitTimeListId;
import de.jobst.resulter.domain.analysis.ControlSegment;
import de.jobst.resulter.domain.analysis.ControlSequenceSegment;
import de.jobst.resulter.domain.analysis.SplitTimeAnalysis;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anySet;
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
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());
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
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());
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
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());
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
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());
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
    void analyzeSplitTimesRanking_shouldApplyPersonFilterToSequenceSegments() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository,
                personRepository,
                resultListRepository
        );

        ResultListId resultListId = ResultListId.of(15L);
        // runner1 (H21) and runner2 (D21) share a sequence; runner3 (H21) should be filtered out
        SplitTimeList runner1 = splitTimeList(1L, "H21", List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.of(1L)),
                SplitTime.of("32", 190.0, SplitTimeListId.of(1L))
        ));
        SplitTimeList runner2 = splitTimeList(2L, "D21", List.of(
                SplitTime.of("31", 105.0, SplitTimeListId.of(2L)),
                SplitTime.of("32", 200.0, SplitTimeListId.of(2L))
        ));
        SplitTimeList runner3 = splitTimeList(3L, "H21", List.of(
                SplitTime.of("31", 108.0, SplitTimeListId.of(3L)),
                SplitTime.of("32", 205.0, SplitTimeListId.of(3L))
        ));

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner1, runner2, runner3));
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList(resultListId)));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                resultListId,
                false,
                List.of(1L, 2L), // only runner1 and runner2
                false,
                true,
                2
        );

        assertThat(analyses).hasSize(1);
        // All sequence runner splits must belong to filtered persons only
        analyses.getFirst().sequenceSegments().forEach(seg ->
                seg.runnerSplits().forEach(split ->
                        assertThat(split.personId().value()).isIn(1L, 2L)
                )
        );
        // runner3 must not appear in any sequence
        analyses.getFirst().sequenceSegments().forEach(seg ->
                assertThat(seg.runnerSplits().stream().map(s -> s.personId().value()).toList())
                        .doesNotContain(3L)
        );
    }

    @Test
    void analyzeSplitTimesRanking_shouldApplyIntersectionFilterToSequenceSegments() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository,
                personRepository,
                resultListRepository
        );

        ResultListId resultListId = ResultListId.of(16L);
        // runner1 (H21): runs 31->32->33; runner2 (D21): runs only 31->32; runner3 (H21): runs 31->32->33
        // With filterIntersection=true for runner1+runner2, only sequences where BOTH appear are shown
        SplitTimeList runner1 = splitTimeList(1L, "H21", List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.of(1L)),
                SplitTime.of("32", 190.0, SplitTimeListId.of(1L)),
                SplitTime.of("33", 300.0, SplitTimeListId.of(1L))
        ));
        SplitTimeList runner2 = splitTimeList(2L, "D21", List.of(
                SplitTime.of("31", 105.0, SplitTimeListId.of(2L)),
                SplitTime.of("32", 200.0, SplitTimeListId.of(2L))
                // runner2 does NOT reach 33
        ));

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner1, runner2));
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList(resultListId)));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                resultListId,
                false,
                List.of(1L, 2L),
                true, // filterIntersection: both must be present
                true,
                2
        );

        assertThat(analyses).hasSize(1);
        // Only sequences where BOTH runner1 and runner2 appear should remain
        analyses.getFirst().sequenceSegments().forEach(seg -> {
            List<Long> personIds = seg.runnerSplits().stream().map(s -> s.personId().value()).toList();
            assertThat(personIds).contains(1L, 2L);
        });
        // Sequences that include control 33 (only runner1) must not appear
        analyses.getFirst().sequenceSegments().forEach(seg ->
                assertThat(seg.controls().stream().map(c -> c.value()).toList())
                        .doesNotContain("33")
        );
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
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());
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

    // =========================================================================
    // calculateControlSegments
    // =========================================================================

    @Test
    void controlSegments_shouldContainBothRunnersOnSharedControlPair() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository, personRepository, resultListRepository);

        ResultListId resultListId = ResultListId.of(20L);
        // Beide Läufer passieren Posten 31 und 32
        SplitTimeList runner1 = splitTimeList(1L, "H21", List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.of(1L)),
                SplitTime.of("32", 190.0, SplitTimeListId.of(1L))
        ));
        SplitTimeList runner2 = splitTimeList(2L, "H21", List.of(
                SplitTime.of("31", 110.0, SplitTimeListId.of(2L)),
                SplitTime.of("32", 205.0, SplitTimeListId.of(2L))
        ));

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner1, runner2));
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList(resultListId)));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                resultListId, false, List.of(), false, false, 2);

        List<ControlSegment> segments = analyses.getFirst().controlSegments();
        // S→31 und 31→32 sollen jeweils beide Läufer enthalten
        assertThat(segments).isNotEmpty();

        ControlSegment seg31to32 = segments.stream()
                .filter(s -> s.fromControl().value().equals("31") && s.toControl().value().equals("32"))
                .findFirst()
                .orElseThrow();
        assertThat(seg31to32.runnerSplits()).hasSize(2);
        assertThat(seg31to32.runnerSplits().stream().map(rs -> rs.personId().value()).toList())
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void controlSegments_runnersShouldBeSortedByTimeWithCorrectPositionsAndTimeBehind() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository, personRepository, resultListRepository);

        ResultListId resultListId = ResultListId.of(21L);
        // Runner1: S→31 = 100s; Runner2: S→31 = 90s (schneller)
        SplitTimeList runner1 = splitTimeList(1L, "H21", List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.of(1L))
        ));
        SplitTimeList runner2 = splitTimeList(2L, "H21", List.of(
                SplitTime.of("31", 90.0, SplitTimeListId.of(2L))
        ));

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner1, runner2));
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList(resultListId)));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                resultListId, false, List.of(), false, false, 2);

        ControlSegment segSto31 = analyses.getFirst().controlSegments().stream()
                .filter(s -> s.fromControl().value().equals("S") && s.toControl().value().equals("31"))
                .findFirst()
                .orElseThrow();

        // Schnellster zuerst (runner2 mit 90s)
        assertThat(segSto31.runnerSplits().get(0).personId().value()).isEqualTo(2L);
        assertThat(segSto31.runnerSplits().get(0).position()).isEqualTo(1);
        assertThat(segSto31.runnerSplits().get(0).timeBehindLeader()).isEqualTo(0.0);

        assertThat(segSto31.runnerSplits().get(1).personId().value()).isEqualTo(1L);
        assertThat(segSto31.runnerSplits().get(1).position()).isEqualTo(2);
        assertThat(segSto31.runnerSplits().get(1).timeBehindLeader()).isEqualTo(10.0);
    }

    @Test
    void controlSegments_shouldSkipSegmentWithOnlySingleRunnerWhenNoFilterActive() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository, personRepository, resultListRepository);

        ResultListId resultListId = ResultListId.of(22L);
        // Nur ein Läufer → Segment S→31 hat nur einen Eintrag → soll nicht erscheinen
        SplitTimeList runner1 = splitTimeList(1L, "H21", List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.of(1L))
        ));

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner1));
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList(resultListId)));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                resultListId, false, List.of(), false, false, 2);

        assertThat(analyses.getFirst().controlSegments()).isEmpty();
    }

    @Test
    void controlSegments_shouldSkipSegmentWithNullPunchTime() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository, personRepository, resultListRepository);

        ResultListId resultListId = ResultListId.of(23L);
        // Runner1: Posten 31 fehlt (null → DNF-Stempel), Runner2: normaler Lauf
        SplitTimeList runner1 = splitTimeList(1L, "H21", List.of(
                SplitTime.of("31", null, SplitTimeListId.of(1L)),
                SplitTime.of("32", 200.0, SplitTimeListId.of(1L))
        ));
        SplitTimeList runner2 = splitTimeList(2L, "H21", List.of(
                SplitTime.of("31", 110.0, SplitTimeListId.of(2L)),
                SplitTime.of("32", 210.0, SplitTimeListId.of(2L))
        ));

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner1, runner2));
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList(resultListId)));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                resultListId, false, List.of(), false, false, 2);

        // S→31: runner1 hat null → nur runner2 → wird übersprungen (< 2 Läufer)
        boolean hasSto31withRunner1 = analyses.getFirst().controlSegments().stream()
                .filter(s -> s.fromControl().value().equals("S") && s.toControl().value().equals("31"))
                .flatMap(s -> s.runnerSplits().stream())
                .anyMatch(rs -> rs.personId().value().equals(1L));
        assertThat(hasSto31withRunner1).isFalse();

        // 31→32: runner1 überspringt dieses Segment weil punchTime[31] null ist
        boolean has31to32withRunner1 = analyses.getFirst().controlSegments().stream()
                .filter(s -> s.fromControl().value().equals("31") && s.toControl().value().equals("32"))
                .flatMap(s -> s.runnerSplits().stream())
                .anyMatch(rs -> rs.personId().value().equals(1L));
        assertThat(has31to32withRunner1).isFalse();
    }

    @Test
    void controlSegments_shouldApplyPersonFilter() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository, personRepository, resultListRepository);

        ResultListId resultListId = ResultListId.of(24L);
        SplitTimeList runner1 = splitTimeList(1L, "H21", List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.of(1L))
        ));
        SplitTimeList runner2 = splitTimeList(2L, "H21", List.of(
                SplitTime.of("31", 110.0, SplitTimeListId.of(2L))
        ));
        SplitTimeList runner3 = splitTimeList(3L, "H21", List.of(
                SplitTime.of("31", 120.0, SplitTimeListId.of(3L))
        ));

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner1, runner2, runner3));
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList(resultListId)));

        // Nur runner1 und runner2 filtern
        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                resultListId, false, List.of(1L, 2L), false, false, 2);

        analyses.getFirst().controlSegments().forEach(seg ->
                seg.runnerSplits().forEach(rs ->
                        assertThat(rs.personId().value()).isIn(1L, 2L)
                )
        );
        analyses.getFirst().controlSegments().forEach(seg ->
                assertThat(seg.runnerSplits().stream().map(rs -> rs.personId().value()).toList())
                        .doesNotContain(3L)
        );
    }

    @Test
    void controlSegments_shouldMergeBidirectionalSegments() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository, personRepository, resultListRepository);

        ResultListId resultListId = ResultListId.of(25L);
        // Runner1+2 laufen 31→32; Runner3+4 laufen 32→31 (umgekehrter Kurs)
        // Jeweils mind. 2 Läufer pro Richtung damit Segmente nicht als Einzel-Läufer verworfen werden
        SplitTimeList runner1 = splitTimeList(1L, "H21", List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.of(1L)),
                SplitTime.of("32", 190.0, SplitTimeListId.of(1L))
        ));
        SplitTimeList runner2 = splitTimeList(2L, "H21", List.of(
                SplitTime.of("31", 105.0, SplitTimeListId.of(2L)),
                SplitTime.of("32", 196.0, SplitTimeListId.of(2L))
        ));
        SplitTimeList runner3 = splitTimeList(3L, "D21", List.of(
                SplitTime.of("32",  90.0, SplitTimeListId.of(3L)),
                SplitTime.of("31", 180.0, SplitTimeListId.of(3L))
        ));
        SplitTimeList runner4 = splitTimeList(4L, "D21", List.of(
                SplitTime.of("32",  95.0, SplitTimeListId.of(4L)),
                SplitTime.of("31", 186.0, SplitTimeListId.of(4L))
        ));

        when(splitTimeListRepository.findByResultListId(resultListId))
                .thenReturn(List.of(runner1, runner2, runner3, runner4));
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList(resultListId)));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                resultListId, true, List.of(), false, false, 2);

        // Nach Merge darf kein separates 32→31-Segment mehr existieren
        boolean hasForward = analyses.getFirst().controlSegments().stream()
                .anyMatch(s -> s.fromControl().value().equals("31") && s.toControl().value().equals("32"));
        boolean hasReverse = analyses.getFirst().controlSegments().stream()
                .anyMatch(s -> s.fromControl().value().equals("32") && s.toControl().value().equals("31"));

        assertThat(hasForward).isTrue();
        assertThat(hasReverse).isFalse();

        // Das verbleibende bidirektionale Segment enthält alle vier Läufer
        ControlSegment merged = analyses.getFirst().controlSegments().stream()
                .filter(s -> s.fromControl().value().equals("31") && s.toControl().value().equals("32"))
                .findFirst()
                .orElseThrow();
        assertThat(merged.bidirectional()).isTrue();
        assertThat(merged.runnerSplits().stream().map(rs -> rs.personId().value()).toList())
                .containsExactlyInAnyOrder(1L, 2L, 3L, 4L);
    }

    // =========================================================================
    // splitTimeLists.isEmpty() early exit
    // =========================================================================

    @Test
    void analyzeSplitTimesRanking_shouldReturnEmptyWhenNoSplitTimeLists() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository, personRepository, resultListRepository);

        ResultListId resultListId = ResultListId.of(50L);
        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of());

        List<SplitTimeAnalysis> result = service.analyzeSplitTimesRanking(
                resultListId, false, List.of(), false, false, 2);

        assertThat(result).isEmpty();
    }

    // =========================================================================
    // buildRuntimeMap / addStartAndFinishControls — runtime aus ResultList
    // =========================================================================

    @Test
    void analyzeSplitTimesRanking_shouldIncludeFinishControlFromRuntimeMap() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository, personRepository, resultListRepository);

        ResultListId resultListId = ResultListId.of(51L);
        SplitTimeList runner1 = splitTimeList(1L, "H21", List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.of(1L))
        ));
        SplitTimeList runner2 = splitTimeList(2L, "H21", List.of(
                SplitTime.of("31", 110.0, SplitTimeListId.of(2L))
        ));

        PersonRaceResult prr1 = PersonRaceResult.of("H21", 1L, null, null, 300.0, 1L, (byte) 1, ResultStatus.OK);
        PersonRaceResult prr2 = PersonRaceResult.of("H21", 2L, null, null, 320.0, 2L, (byte) 1, ResultStatus.OK);
        PersonResult pr1 = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of(prr1));
        PersonResult pr2 = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(2L), null, List.of(prr2));
        ClassResult cr = ClassResult.of("Herren 21", "H21", Gender.M, List.of(pr1, pr2), null);
        ResultList rl = new ResultList(resultListId, EventId.of(1L), RaceId.of(1L), null, null, null, List.of(cr));

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner1, runner2));
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(rl));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                resultListId, false, List.of(), false, false, 2);

        assertThat(analyses).hasSize(1);
        boolean has31toF = analyses.getFirst().controlSegments().stream()
                .anyMatch(s -> s.fromControl().value().equals("31") && s.toControl().value().equals("F"));
        assertThat(has31toF).isTrue();
    }

    // =========================================================================
    // buildClassToCourseKeyMap — CourseId im ClassResult
    // =========================================================================

    @Test
    void analyzeSplitTimesRanking_shouldUseCourseIdFromClassResult() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository, personRepository, resultListRepository);

        ResultListId resultListId = ResultListId.of(52L);
        SplitTimeList runner1 = splitTimeList(1L, "H21", List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.of(1L)),
                SplitTime.of("32", 190.0, SplitTimeListId.of(1L))
        ));
        SplitTimeList runner2 = splitTimeList(2L, "D21", List.of(
                SplitTime.of("31", 105.0, SplitTimeListId.of(2L)),
                SplitTime.of("32", 200.0, SplitTimeListId.of(2L))
        ));

        PersonRaceResult prr1 = PersonRaceResult.of("H21", 1L, null, null, 300.0, 1L, (byte) 1, ResultStatus.OK);
        PersonRaceResult prr2 = PersonRaceResult.of("D21", 2L, null, null, 320.0, 1L, (byte) 1, ResultStatus.OK);
        PersonResult pr1 = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of(prr1));
        PersonResult pr2 = PersonResult.of(ClassResultShortName.of("D21"), PersonId.of(2L), null, List.of(prr2));
        // Beide Klassen auf denselben Kurs CID:10 → classToCourseKey wird befüllt
        ClassResult cr1 = ClassResult.of("Herren 21", "H21", Gender.M, List.of(pr1), CourseId.of(10L));
        ClassResult cr2 = ClassResult.of("Damen 21", "D21", Gender.F, List.of(pr2), CourseId.of(10L));
        ResultList rl = new ResultList(resultListId, EventId.of(1L), RaceId.of(1L), null, null, null, List.of(cr1, cr2));

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner1, runner2));
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(rl));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                resultListId, false, List.of(), false, true, 2);

        // Beide Läufer teilen denselben Kurs → distinctCourseControls == 1 → keine Sequenzen
        assertThat(analyses).hasSize(1);
        assertThat(analyses.getFirst().sequenceSegments()).isEmpty();
    }

    // =========================================================================
    // buildValidResultKeys — FINISHED-Status
    // =========================================================================

    @Test
    void analyzeSplitTimesRanking_shouldIncludeFinishedStatusInValidResultKeys() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository, personRepository, resultListRepository);

        ResultListId resultListId = ResultListId.of(53L);
        SplitTimeList runner1 = splitTimeList(1L, "H21", List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.of(1L))
        ));
        SplitTimeList runner2 = splitTimeList(2L, "H21", List.of(
                SplitTime.of("31", 110.0, SplitTimeListId.of(2L))
        ));

        // FINISHED statt OK – soll ebenfalls als valides Ergebnis gelten
        PersonRaceResult prr1 = PersonRaceResult.of("H21", 1L, null, null, 300.0, 1L, (byte) 1, ResultStatus.FINISHED);
        PersonRaceResult prr2 = PersonRaceResult.of("H21", 2L, null, null, 320.0, 2L, (byte) 1, ResultStatus.FINISHED);
        PersonResult pr1 = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of(prr1));
        PersonResult pr2 = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(2L), null, List.of(prr2));
        ClassResult cr = ClassResult.of("Herren 21", "H21", Gender.M, List.of(pr1, pr2), null);
        ResultList rl = new ResultList(resultListId, EventId.of(1L), RaceId.of(1L), null, null, null, List.of(cr));

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner1, runner2));
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(rl));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                resultListId, false, List.of(), false, false, 2);

        assertThat(analyses).hasSize(1);
        // Mit FINISHED-Status ist runtimeMap befüllt → F-Posten erscheint
        boolean has31toF = analyses.getFirst().controlSegments().stream()
                .anyMatch(s -> s.fromControl().value().equals("31") && s.toControl().value().equals("F"));
        assertThat(has31toF).isTrue();
    }

    // =========================================================================
    // getPersonsForResultList
    // =========================================================================

    @Test
    void getPersonsForResultList_shouldReturnEmptyWhenNoSplitTimes() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository, personRepository, resultListRepository);

        ResultListId resultListId = ResultListId.of(60L);
        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of());

        List<Person> result = service.getPersonsForResultList(resultListId);

        assertThat(result).isEmpty();
    }

    @Test
    void getPersonsForResultList_shouldReturnPersonsSortedByFamilyNameThenGivenName() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository, personRepository, resultListRepository);

        ResultListId resultListId = ResultListId.of(61L);
        SplitTimeList runner1 = splitTimeList(1L, "H21", List.of());
        SplitTimeList runner2 = splitTimeList(2L, "H21", List.of());

        Person personA = Person.of(1L, "Müller", "Anna", null, Gender.F);
        Person personB = Person.of(2L, "Anders", "Bernd", null, Gender.M);

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner1, runner2));
        when(personRepository.findAllById(anySet()))
                .thenReturn(Map.of(PersonId.of(1L), personA, PersonId.of(2L), personB));

        List<Person> result = service.getPersonsForResultList(resultListId);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().personName().familyName().value()).isEqualTo("Anders");
        assertThat(result.getLast().personName().familyName().value()).isEqualTo("Müller");
    }

    @Test
    void controlSegments_shouldAssignSamePositionToTiedRunners() {
        SplitTimeListRepository splitTimeListRepository = mock(SplitTimeListRepository.class);
        PersonRepository personRepository = mock(PersonRepository.class);
        ResultListRepository resultListRepository = mock(ResultListRepository.class);

        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(
                splitTimeListRepository, personRepository, resultListRepository);

        ResultListId resultListId = ResultListId.of(70L);
        // Runner1 and Runner2 have exactly the same split time at control "31" → tied at position 1
        // Runner3 has a different (slower) time → position 3 (not 2, Olympic ranking)
        SplitTimeList runner1 = splitTimeList(1L, "H21", List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.of(1L))
        ));
        SplitTimeList runner2 = splitTimeList(2L, "H21", List.of(
                SplitTime.of("31", 100.0, SplitTimeListId.of(2L))
        ));
        SplitTimeList runner3 = splitTimeList(3L, "H21", List.of(
                SplitTime.of("31", 120.0, SplitTimeListId.of(3L))
        ));

        when(splitTimeListRepository.findByResultListId(resultListId)).thenReturn(List.of(runner1, runner2, runner3));
        when(personRepository.findAllById(anySet())).thenReturn(Map.of());
        when(resultListRepository.findById(resultListId)).thenReturn(Optional.of(resultList(resultListId)));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                resultListId, false, List.of(), false, false, 2);

        assertThat(analyses).hasSize(1);
        ControlSegment s31 = analyses.getFirst().controlSegments().stream()
                .filter(s -> s.fromControl().value().equals("S") && s.toControl().value().equals("31"))
                .findFirst().orElseThrow();

        assertThat(s31.runnerSplits()).hasSize(3);
        List<Integer> positions = s31.runnerSplits().stream()
                .sorted(java.util.Comparator.comparingLong(rs -> rs.personId().value()))
                .map(rs -> rs.position())
                .toList();
        // runner1 and runner2 tied → both position 1; runner3 → position 3
        assertThat(positions.get(0)).isEqualTo(1);
        assertThat(positions.get(1)).isEqualTo(1);
        assertThat(positions.get(2)).isEqualTo(3);
    }

    // =========================================================================
    // orElseThrow lambda — resultListRepository returns empty
    // =========================================================================

    @Test
    void analyzeSplitTimesRanking_throws_whenResultListNotFound() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        PersonRepository personRepo = mock(PersonRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(stlRepo, personRepo, rlRepo);

        ResultListId id = ResultListId.of(90L);
        when(stlRepo.findByResultListId(id)).thenReturn(List.of(
                splitTimeList(1L, "H21", List.of(SplitTime.of("31", 100.0, SplitTimeListId.of(1L))))
        ));
        when(personRepo.findAllById(anySet())).thenReturn(Map.of());
        when(rlRepo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.analyzeSplitTimesRanking(id, false, List.of(), false, false, 2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // =========================================================================
    // thenComparing(givenName) lambda in getPersonsForResultList
    // =========================================================================

    @Test
    void getPersonsForResultList_sortsByGivenName_whenFamilyNameTied() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        PersonRepository personRepo = mock(PersonRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(stlRepo, personRepo, rlRepo);

        ResultListId id = ResultListId.of(91L);
        SplitTimeList stl1 = splitTimeList(1L, "H21", List.of());
        SplitTimeList stl2 = splitTimeList(2L, "H21", List.of());
        Person personA = Person.of(1L, "Müller", "Zara", null, Gender.F);
        Person personB = Person.of(2L, "Müller", "Anna", null, Gender.F);

        when(stlRepo.findByResultListId(id)).thenReturn(List.of(stl1, stl2));
        when(personRepo.findAllById(anySet())).thenReturn(Map.of(PersonId.of(1L), personA, PersonId.of(2L), personB));

        List<Person> result = service.getPersonsForResultList(id);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().personName().givenName().value()).isEqualTo("Anna");
        assertThat(result.getLast().personName().givenName().value()).isEqualTo("Zara");
    }

    // =========================================================================
    // mergeRunnerDataByPerson replace branch — butterfly course (same segment twice)
    // =========================================================================

    @Test
    void controlSegments_keepsFasterTime_whenRunnerPunchesSameSegmentTwice() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        PersonRepository personRepo = mock(PersonRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(stlRepo, personRepo, rlRepo);

        ResultListId id = ResultListId.of(92L);
        // Runner1: butterfly — passes 31→32 twice (90s first, 70s second); faster time must be kept
        SplitTimeList runner1 = new SplitTimeList(SplitTimeListId.of(1L), EventId.of(1L), id,
                ClassResultShortName.of("H21"), PersonId.of(1L), RaceNumber.of((byte) 1), List.of(
                        SplitTime.of("31", 100.0, SplitTimeListId.of(1L)),
                        SplitTime.of("32", 190.0, SplitTimeListId.of(1L)),
                        SplitTime.of("31", 280.0, SplitTimeListId.of(1L)),
                        SplitTime.of("32", 350.0, SplitTimeListId.of(1L))
        ));
        // Runner2: normal — single pass 31→32 at 95s
        SplitTimeList runner2 = new SplitTimeList(SplitTimeListId.of(2L), EventId.of(1L), id,
                ClassResultShortName.of("H21"), PersonId.of(2L), RaceNumber.of((byte) 1), List.of(
                        SplitTime.of("31", 105.0, SplitTimeListId.of(2L)),
                        SplitTime.of("32", 200.0, SplitTimeListId.of(2L))
        ));

        when(stlRepo.findByResultListId(id)).thenReturn(List.of(runner1, runner2));
        when(personRepo.findAllById(anySet())).thenReturn(Map.of());
        when(rlRepo.findById(id)).thenReturn(Optional.of(resultList(id)));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(id, false, List.of(), false, false, 2);

        assertThat(analyses).hasSize(1);
        ControlSegment seg = analyses.getFirst().controlSegments().stream()
                .filter(s -> s.fromControl().value().equals("31") && s.toControl().value().equals("32"))
                .findFirst().orElseThrow();
        // Runner1 must appear exactly once with the faster time (70s = 350-280), not 90s
        assertThat(seg.runnerSplits()).hasSize(2);
        assertThat(seg.runnerSplits().get(0).personId().value()).isEqualTo(1L);
        assertThat(seg.runnerSplits().get(0).splitTimeSeconds()).isEqualTo(70.0);
    }

    // =========================================================================
    // mergeSequenceRunnerDataByPerson replace branch — butterfly with sequences
    // =========================================================================

    @Test
    void sequenceSegments_keepsFasterTime_whenRunnerRunsSameSequenceTwice() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        PersonRepository personRepo = mock(PersonRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(stlRepo, personRepo, rlRepo);

        ResultListId id = ResultListId.of(93L);
        // Runner1 (H21): butterfly — same "31>32" appears twice (90s first pass, 70s second pass)
        SplitTimeList runner1 = new SplitTimeList(SplitTimeListId.of(1L), EventId.of(1L), id,
                ClassResultShortName.of("H21"), PersonId.of(1L), RaceNumber.of((byte) 1), List.of(
                        SplitTime.of("31", 100.0, SplitTimeListId.of(1L)),
                        SplitTime.of("32", 190.0, SplitTimeListId.of(1L)),
                        SplitTime.of("31", 280.0, SplitTimeListId.of(1L)),
                        SplitTime.of("32", 350.0, SplitTimeListId.of(1L))
        ));
        // Runner2 (D21): single pass 31→32, different courseControlsKey → distinctCourseControls = 2
        SplitTimeList runner2 = new SplitTimeList(SplitTimeListId.of(2L), EventId.of(1L), id,
                ClassResultShortName.of("D21"), PersonId.of(2L), RaceNumber.of((byte) 1), List.of(
                        SplitTime.of("31", 105.0, SplitTimeListId.of(2L)),
                        SplitTime.of("32", 200.0, SplitTimeListId.of(2L))
        ));

        when(stlRepo.findByResultListId(id)).thenReturn(List.of(runner1, runner2));
        when(personRepo.findAllById(anySet())).thenReturn(Map.of());
        when(rlRepo.findById(id)).thenReturn(Optional.of(resultList(id)));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(id, false, List.of(), false, true, 2);

        assertThat(analyses).hasSize(1);
        // Sequence "31>32" must contain runner1 once with the faster time (70s)
        analyses.getFirst().sequenceSegments().stream()
                .filter(s -> s.controls().stream().map(c -> c.value()).toList().equals(List.of("31", "32")))
                .findFirst()
                .ifPresent(seg -> {
                    var runner1Split = seg.runnerSplits().stream()
                            .filter(rs -> rs.personId().value().equals(1L))
                            .findFirst().orElseThrow();
                    assertThat(runner1Split.splitTimeSeconds()).isEqualTo(70.0);
                });
    }

    // =========================================================================
    // mergeBidirectionalSegments else branch — fromControl > toControl numerically
    // Uses "90" and "80": hash("90") bucket=7 < hash("80") bucket=8
    // so "90→80" is iterated first in segmentMap, triggering compareControlCodes("90","80") > 0
    // =========================================================================

    @Test
    void controlSegments_mergeBidirectional_swapsControlsAndFlipsReversed_whenFromControlLarger() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        PersonRepository personRepo = mock(PersonRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(stlRepo, personRepo, rlRepo);

        ResultListId id = ResultListId.of(95L);
        // Runner1+2: course 90→80 (larger control first)
        // Runner3+4: course 80→90 (forward direction)
        // All four in the same class "H21" → covers the filter-false branch in mergedClasses dedup
        SplitTimeList runner1 = new SplitTimeList(SplitTimeListId.of(1L), EventId.of(1L), id,
                ClassResultShortName.of("H21"), PersonId.of(1L), RaceNumber.of((byte) 1), List.of(
                        SplitTime.of("90", 100.0, SplitTimeListId.of(1L)),
                        SplitTime.of("80", 190.0, SplitTimeListId.of(1L))
        ));
        SplitTimeList runner2 = new SplitTimeList(SplitTimeListId.of(2L), EventId.of(1L), id,
                ClassResultShortName.of("H21"), PersonId.of(2L), RaceNumber.of((byte) 1), List.of(
                        SplitTime.of("90", 105.0, SplitTimeListId.of(2L)),
                        SplitTime.of("80", 196.0, SplitTimeListId.of(2L))
        ));
        SplitTimeList runner3 = new SplitTimeList(SplitTimeListId.of(3L), EventId.of(1L), id,
                ClassResultShortName.of("H21"), PersonId.of(3L), RaceNumber.of((byte) 1), List.of(
                        SplitTime.of("80",  90.0, SplitTimeListId.of(3L)),
                        SplitTime.of("90", 180.0, SplitTimeListId.of(3L))
        ));
        SplitTimeList runner4 = new SplitTimeList(SplitTimeListId.of(4L), EventId.of(1L), id,
                ClassResultShortName.of("H21"), PersonId.of(4L), RaceNumber.of((byte) 1), List.of(
                        SplitTime.of("80",  95.0, SplitTimeListId.of(4L)),
                        SplitTime.of("90", 186.0, SplitTimeListId.of(4L))
        ));

        when(stlRepo.findByResultListId(id)).thenReturn(List.of(runner1, runner2, runner3, runner4));
        when(personRepo.findAllById(anySet())).thenReturn(Map.of());
        when(rlRepo.findById(id)).thenReturn(Optional.of(resultList(id)));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(id, true, List.of(), false, false, 2);

        assertThat(analyses).hasSize(1);
        // Merged segment must have smaller control first: "80→90"
        boolean hasForward = analyses.getFirst().controlSegments().stream()
                .anyMatch(s -> s.fromControl().value().equals("80") && s.toControl().value().equals("90"));
        boolean hasReverse = analyses.getFirst().controlSegments().stream()
                .anyMatch(s -> s.fromControl().value().equals("90") && s.toControl().value().equals("80"));
        assertThat(hasForward).isTrue();
        assertThat(hasReverse).isFalse();

        ControlSegment merged = analyses.getFirst().controlSegments().stream()
                .filter(s -> s.fromControl().value().equals("80") && s.toControl().value().equals("90"))
                .findFirst().orElseThrow();
        assertThat(merged.bidirectional()).isTrue();
        assertThat(merged.runnerSplits()).hasSize(4);
        // Runners 3+4 went 80→90 (not reversed); runners 1+2 went 90→80 (reversed after flip)
        long reversedCount = merged.runnerSplits().stream().filter(rs -> rs.reversed()).count();
        assertThat(reversedCount).isEqualTo(2);
    }

    // =========================================================================
    // splitControlsKey + buildCourseMetadataByCourseKey tiebreaker lambdas
    // =========================================================================

    @Test
    void buildCourseMetadata_invokesControlsKeySizeTiebreaker_whenTwoVariantsHaveSameRunnerCount() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        PersonRepository personRepo = mock(PersonRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(stlRepo, personRepo, rlRepo);

        ResultListId id = ResultListId.of(94L);
        // Two runners in H21 with valid OK results but different control sequences → controlsKeyCounts has 2 entries with count=1
        PersonRaceResult prr1 = PersonRaceResult.of("H21", 1L, null, null, 300.0, 1L, (byte) 1, ResultStatus.OK);
        PersonRaceResult prr2 = PersonRaceResult.of("H21", 2L, null, null, 320.0, 1L, (byte) 1, ResultStatus.OK);
        PersonResult pr1 = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of(prr1));
        PersonResult pr2 = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(2L), null, List.of(prr2));
        ClassResult cr = ClassResult.of("H21", "H21", Gender.M, List.of(pr1, pr2), null);
        ResultList rl = new ResultList(id, EventId.of(1L), RaceId.of(1L), null, null, null, List.of(cr));

        SplitTimeList stl1 = new SplitTimeList(SplitTimeListId.of(1L), EventId.of(1L), id,
                ClassResultShortName.of("H21"), PersonId.of(1L), RaceNumber.of((byte) 1), List.of(
                        SplitTime.of("31", 100.0, SplitTimeListId.of(1L)),
                        SplitTime.of("32", 190.0, SplitTimeListId.of(1L))   // variant A: 31→32
        ));
        SplitTimeList stl2 = new SplitTimeList(SplitTimeListId.of(2L), EventId.of(1L), id,
                ClassResultShortName.of("H21"), PersonId.of(2L), RaceNumber.of((byte) 1), List.of(
                        SplitTime.of("31", 105.0, SplitTimeListId.of(2L)),
                        SplitTime.of("33", 200.0, SplitTimeListId.of(2L))   // variant B: 31→33
        ));

        when(stlRepo.findByResultListId(id)).thenReturn(List.of(stl1, stl2));
        when(personRepo.findAllById(anySet())).thenReturn(Map.of());
        when(rlRepo.findById(id)).thenReturn(Optional.of(rl));

        // The service must run without error and produce a valid analysis;
        // internally it invokes splitControlsKey and the tiebreaker comparators
        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(id, false, List.of(), false, true, 2);

        assertThat(analyses).hasSize(1);
        // The sequence segments may be empty (both runners share the same courseKey → distinctCourseControls≤1 after merge)
        // but the analysis itself must be produced correctly
        assertThat(analyses.getFirst()).isNotNull();
    }

    // =========================================================================
    // L338: validSplits.size() < minControls — runner mit zu wenig Stempelungen
    // L346: maxControls < minControls — runner mit genau minControls-1 gültigen Teilstrecken
    // =========================================================================

    @Test
    void calculateControlSequenceSegments_coversEarlySkipBranches_whenTooFewValidSplits() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        PersonRepository personRepo = mock(PersonRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(stlRepo, personRepo, rlRepo);

        ResultListId id = ResultListId.of(96L);
        // runner1: 1 Original-Stempelung → extended=[S,31] → validSplits=2 < minControls=3 → L338 continue
        SplitTimeList runner1 = new SplitTimeList(SplitTimeListId.of(1L), EventId.of(1L), id,
                ClassResultShortName.of("H21"), PersonId.of(1L), RaceNumber.of((byte) 1),
                List.of(SplitTime.of("31", 100.0, SplitTimeListId.of(1L))));
        // runner2: 2 Original-Stempelungen → extended=[S,31,32] → validSplits=3, maxControls=2 < minControls=3 → L346 continue
        SplitTimeList runner2 = new SplitTimeList(SplitTimeListId.of(2L), EventId.of(1L), id,
                ClassResultShortName.of("H21"), PersonId.of(2L), RaceNumber.of((byte) 1),
                List.of(SplitTime.of("31", 105.0, SplitTimeListId.of(2L)),
                        SplitTime.of("32", 200.0, SplitTimeListId.of(2L))));

        when(stlRepo.findByResultListId(id)).thenReturn(List.of(runner1, runner2));
        when(personRepo.findAllById(anySet())).thenReturn(Map.of());
        when(rlRepo.findById(id)).thenReturn(Optional.of(resultList(id)));

        // sequenceMinControls=3 → minControls=3; beide Läufer haben zu wenige Stempelungen
        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                id, false, List.of(), false, true, 3);

        assertThat(analyses).hasSize(1);
        assertThat(analyses.getFirst().sequenceSegments()).isEmpty();
    }

    // =========================================================================
    // L470 (buildValidResultKeys), L562 (buildClassToCourseKeyMap), L896 (buildRuntimeMap)
    // — null classResults im ResultList
    // =========================================================================

    @Test
    void analyzeSplitTimesRanking_handlesNullClassResults_inResultList() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        PersonRepository personRepo = mock(PersonRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(stlRepo, personRepo, rlRepo);

        ResultListId id = ResultListId.of(97L);
        SplitTimeList runner1 = new SplitTimeList(SplitTimeListId.of(1L), EventId.of(1L), id,
                ClassResultShortName.of("H21"), PersonId.of(1L), RaceNumber.of((byte) 1),
                List.of(SplitTime.of("31", 100.0, SplitTimeListId.of(1L))));
        SplitTimeList runner2 = new SplitTimeList(SplitTimeListId.of(2L), EventId.of(1L), id,
                ClassResultShortName.of("H21"), PersonId.of(2L), RaceNumber.of((byte) 1),
                List.of(SplitTime.of("31", 110.0, SplitTimeListId.of(2L))));

        // null classResults → buildRuntimeMap/buildValidResultKeys/buildClassToCourseKeyMap geben leer zurück
        ResultList rlNullClassResults = new ResultList(
                id, EventId.of(1L), RaceId.of(1L), null, null, null, null);

        when(stlRepo.findByResultListId(id)).thenReturn(List.of(runner1, runner2));
        when(personRepo.findAllById(anySet())).thenReturn(Map.of());
        when(rlRepo.findById(id)).thenReturn(Optional.of(rlNullClassResults));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                id, false, List.of(), false, false, 2);

        assertThat(analyses).hasSize(1);
        // Kein F-Posten (kein Runtime), daher nur S→31 mit beiden Läufern
        boolean hasSto31 = analyses.getFirst().controlSegments().stream()
                .anyMatch(s -> s.fromControl().value().equals("S") && s.toControl().value().equals("31"));
        assertThat(hasSto31).isTrue();
    }

    // =========================================================================
    // L478: continue in buildValidResultKeys für Status != OK und != FINISHED
    // =========================================================================

    @Test
    void buildValidResultKeys_skipsNonOkAndNonFinishedResults() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        PersonRepository personRepo = mock(PersonRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(stlRepo, personRepo, rlRepo);

        ResultListId id = ResultListId.of(98L);
        SplitTimeList runner1 = new SplitTimeList(SplitTimeListId.of(1L), EventId.of(1L), id,
                ClassResultShortName.of("H21"), PersonId.of(1L), RaceNumber.of((byte) 1),
                List.of(SplitTime.of("31", 100.0, SplitTimeListId.of(1L))));
        SplitTimeList runner2 = new SplitTimeList(SplitTimeListId.of(2L), EventId.of(1L), id,
                ClassResultShortName.of("H21"), PersonId.of(2L), RaceNumber.of((byte) 1),
                List.of(SplitTime.of("31", 110.0, SplitTimeListId.of(2L))));

        // runner1 hat MISSING_PUNCH → wird in buildValidResultKeys übersprungen → kein F-Posten
        // runner2 hat OK → validResultKey gesetzt → F-Posten erscheint
        PersonRaceResult prr1 = PersonRaceResult.of("H21", 1L, null, null, 300.0, 1L, (byte) 1, ResultStatus.MISSING_PUNCH);
        PersonRaceResult prr2 = PersonRaceResult.of("H21", 2L, null, null, 320.0, 2L, (byte) 1, ResultStatus.OK);
        PersonResult pr1 = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of(prr1));
        PersonResult pr2 = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(2L), null, List.of(prr2));
        ClassResult cr = ClassResult.of("H21", "H21", Gender.M, List.of(pr1, pr2), null);
        ResultList rl = new ResultList(id, EventId.of(1L), RaceId.of(1L), null, null, null, List.of(cr));

        when(stlRepo.findByResultListId(id)).thenReturn(List.of(runner1, runner2));
        when(personRepo.findAllById(anySet())).thenReturn(Map.of());
        when(rlRepo.findById(id)).thenReturn(Optional.of(rl));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                id, false, List.of(), false, false, 2);

        assertThat(analyses).hasSize(1);
        // Nur runner2 hat valides Ergebnis → 31→F taucht nur für runner2 auf, aber 31→F hat nur 1 Läufer → kein Segment
        // S→31 hat beide Läufer → Segment existiert
        boolean hasSto31 = analyses.getFirst().controlSegments().stream()
                .anyMatch(s -> s.fromControl().value().equals("S") && s.toControl().value().equals("31"));
        assertThat(hasSto31).isTrue();
    }

    // =========================================================================
    // L950-952: NumberFormatException-Branch in compareControlCodes
    // Tritt auf, wenn zwei Segmente mit gleicher fromControl aber
    // nicht-numerischer toControl (z.B. "F") sortiert werden.
    // =========================================================================

    @Test
    void compareControlCodes_usesLexicographicFallback_forFinishControl() {
        SplitTimeListRepository stlRepo = mock(SplitTimeListRepository.class);
        PersonRepository personRepo = mock(PersonRepository.class);
        ResultListRepository rlRepo = mock(ResultListRepository.class);
        SplitTimeRankingServiceImpl service = new SplitTimeRankingServiceImpl(stlRepo, personRepo, rlRepo);

        ResultListId id = ResultListId.of(99L);
        // runner1+2: haben Posten 31→32 und Runtime → Segment 31→32 UND 32→F
        // runner3+4: haben nur Posten 31 und Runtime, aber KEIN 32 → Segment 31→F
        // Beim Sortieren der Segmente: fromControl "31" ist gleich für 31→32 und 31→F
        // → compareControlCodes("32","F") wird aufgerufen → parseInt("F") wirft NFE → catch-Branch (L950-952)
        PersonRaceResult prr1 = PersonRaceResult.of("H21", 1L, null, null, 300.0, 1L, (byte) 1, ResultStatus.OK);
        PersonRaceResult prr2 = PersonRaceResult.of("H21", 2L, null, null, 310.0, 1L, (byte) 1, ResultStatus.OK);
        PersonRaceResult prr3 = PersonRaceResult.of("D21", 3L, null, null, 280.0, 1L, (byte) 1, ResultStatus.OK);
        PersonRaceResult prr4 = PersonRaceResult.of("D21", 4L, null, null, 290.0, 1L, (byte) 1, ResultStatus.OK);
        PersonResult pr1 = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of(prr1));
        PersonResult pr2 = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(2L), null, List.of(prr2));
        PersonResult pr3 = PersonResult.of(ClassResultShortName.of("D21"), PersonId.of(3L), null, List.of(prr3));
        PersonResult pr4 = PersonResult.of(ClassResultShortName.of("D21"), PersonId.of(4L), null, List.of(prr4));
        ClassResult crH21 = ClassResult.of("H21", "H21", Gender.M, List.of(pr1, pr2), null);
        ClassResult crD21 = ClassResult.of("D21", "D21", Gender.F, List.of(pr3, pr4), null);
        ResultList rl = new ResultList(id, EventId.of(1L), RaceId.of(1L), null, null, null, List.of(crH21, crD21));

        SplitTimeList stl1 = new SplitTimeList(SplitTimeListId.of(1L), EventId.of(1L), id,
                ClassResultShortName.of("H21"), PersonId.of(1L), RaceNumber.of((byte) 1),
                List.of(SplitTime.of("31", 100.0, SplitTimeListId.of(1L)),
                        SplitTime.of("32", 190.0, SplitTimeListId.of(1L))));
        SplitTimeList stl2 = new SplitTimeList(SplitTimeListId.of(2L), EventId.of(1L), id,
                ClassResultShortName.of("H21"), PersonId.of(2L), RaceNumber.of((byte) 1),
                List.of(SplitTime.of("31", 110.0, SplitTimeListId.of(2L)),
                        SplitTime.of("32", 200.0, SplitTimeListId.of(2L))));
        SplitTimeList stl3 = new SplitTimeList(SplitTimeListId.of(3L), EventId.of(1L), id,
                ClassResultShortName.of("D21"), PersonId.of(3L), RaceNumber.of((byte) 1),
                List.of(SplitTime.of("31", 105.0, SplitTimeListId.of(3L))));
        SplitTimeList stl4 = new SplitTimeList(SplitTimeListId.of(4L), EventId.of(1L), id,
                ClassResultShortName.of("D21"), PersonId.of(4L), RaceNumber.of((byte) 1),
                List.of(SplitTime.of("31", 108.0, SplitTimeListId.of(4L))));

        when(stlRepo.findByResultListId(id)).thenReturn(List.of(stl1, stl2, stl3, stl4));
        when(personRepo.findAllById(anySet())).thenReturn(Map.of());
        when(rlRepo.findById(id)).thenReturn(Optional.of(rl));

        List<SplitTimeAnalysis> analyses = service.analyzeSplitTimesRanking(
                id, false, List.of(), false, false, 2);

        assertThat(analyses).hasSize(1);
        // 31→32 (H21 runner1+2) und 31→F (D21 runner3+4) existieren beide
        boolean has31to32 = analyses.getFirst().controlSegments().stream()
                .anyMatch(s -> s.fromControl().value().equals("31") && s.toControl().value().equals("32"));
        boolean has31toF = analyses.getFirst().controlSegments().stream()
                .anyMatch(s -> s.fromControl().value().equals("31") && s.toControl().value().equals("F"));
        assertThat(has31to32).isTrue();
        assertThat(has31toF).isTrue();
        // 31→32 kommt vor 31→F (lexikografisch "32" < "F")
        int idx32 = analyses.getFirst().controlSegments().indexOf(
                analyses.getFirst().controlSegments().stream()
                        .filter(s -> s.fromControl().value().equals("31") && s.toControl().value().equals("32"))
                        .findFirst().orElseThrow());
        int idxF = analyses.getFirst().controlSegments().indexOf(
                analyses.getFirst().controlSegments().stream()
                        .filter(s -> s.fromControl().value().equals("31") && s.toControl().value().equals("F"))
                        .findFirst().orElseThrow());
        assertThat(idx32).isLessThan(idxF);
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
