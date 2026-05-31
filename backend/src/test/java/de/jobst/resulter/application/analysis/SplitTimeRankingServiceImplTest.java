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
import de.jobst.resulter.domain.analysis.ControlSegment;
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
        when(personRepository.findAllById(org.mockito.ArgumentMatchers.anySet())).thenReturn(Map.of());
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
        when(personRepository.findAllById(org.mockito.ArgumentMatchers.anySet())).thenReturn(Map.of());
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
        when(personRepository.findAllById(org.mockito.ArgumentMatchers.anySet())).thenReturn(Map.of());
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
        when(personRepository.findAllById(org.mockito.ArgumentMatchers.anySet())).thenReturn(Map.of());
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
        when(personRepository.findAllById(org.mockito.ArgumentMatchers.anySet())).thenReturn(Map.of());
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
        when(personRepository.findAllById(org.mockito.ArgumentMatchers.anySet())).thenReturn(Map.of());
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
        when(personRepository.findAllById(org.mockito.ArgumentMatchers.anySet())).thenReturn(Map.of());
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
        when(personRepository.findAllById(org.mockito.ArgumentMatchers.anySet())).thenReturn(Map.of());
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
