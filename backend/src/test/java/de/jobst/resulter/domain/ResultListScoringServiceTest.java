package de.jobst.resulter.domain;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResultListScoringServiceTest {

    @Test
    void resultListScoringService_canBeInstantiated() {
        assertThat(new ResultListScoringService()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // isScorableForCupCalculation — Einzelergebnisliste
    // -------------------------------------------------------------------------

    @Test
    void isScorableForCupCalculation_singleResultList_alwaysScorable() {
        ResultList rl = rlWithRaceNum((byte) 1);
        Event event = Event.of("Test Event");

        assertThat(ResultListScoringService.isScorableForCupCalculation(rl, List.of(rl), event)).isTrue();
    }

    // -------------------------------------------------------------------------
    // isScorableForCupCalculation — mehrere Ergebnislisten, aggregatedScore=true
    // -------------------------------------------------------------------------

    @Test
    void isScorableForCupCalculation_multipleResultLists_aggregated_race0IsScorableOnly() {
        ResultList race0 = rlWithRaceNum((byte) 0);
        ResultList race1 = rlWithRaceNum((byte) 1);
        Event aggregatedEvent = aggregatedEvent();
        List<ResultList> all = List.of(race0, race1);

        assertThat(ResultListScoringService.isScorableForCupCalculation(race0, all, aggregatedEvent)).isTrue();
        assertThat(ResultListScoringService.isScorableForCupCalculation(race1, all, aggregatedEvent)).isFalse();
    }

    // -------------------------------------------------------------------------
    // isScorableForCupCalculation — mehrere Ergebnislisten, aggregatedScore=false
    // -------------------------------------------------------------------------

    @Test
    void isScorableForCupCalculation_multipleResultLists_nonAggregated_race0NotScorable() {
        ResultList race0 = rlWithRaceNum((byte) 0);
        ResultList race1 = rlWithRaceNum((byte) 1);
        Event normalEvent = Event.of("Test Event");
        List<ResultList> all = List.of(race0, race1);

        assertThat(ResultListScoringService.isScorableForCupCalculation(race0, all, normalEvent)).isFalse();
        assertThat(ResultListScoringService.isScorableForCupCalculation(race1, all, normalEvent)).isTrue();
    }

    @Test
    void isScorableForCupCalculation_multipleResultLists_nonAggregated_higherRaceNumbersScorable() {
        ResultList race2 = rlWithRaceNum((byte) 2);
        ResultList race3 = rlWithRaceNum((byte) 3);
        Event normalEvent = Event.of("Test Event");
        List<ResultList> all = List.of(race2, race3);

        assertThat(ResultListScoringService.isScorableForCupCalculation(race2, all, normalEvent)).isTrue();
        assertThat(ResultListScoringService.isScorableForCupCalculation(race3, all, normalEvent)).isTrue();
    }

    // -------------------------------------------------------------------------
    // isScorableForCupCalculation — null-Rennnummer
    // -------------------------------------------------------------------------

    @Test
    void isScorableForCupCalculation_multipleResultLists_nullRaceNumber_returnsFalse() {
        ResultList rlNull = rlWithNullRaceNum();
        ResultList rl1 = rlWithRaceNum((byte) 1);
        Event event = Event.of("Test Event");
        List<ResultList> all = List.of(rlNull, rl1);

        assertThat(ResultListScoringService.isScorableForCupCalculation(rlNull, all, event)).isFalse();
    }

    // -------------------------------------------------------------------------
    // shouldDeleteEventWide — Einzelergebnisliste
    // -------------------------------------------------------------------------

    @Test
    void shouldDeleteEventWide_singleResultList_returnsTrue() {
        ResultList rl = rlWithCreateTime(ZonedDateTime.now());

        assertThat(ResultListScoringService.shouldDeleteEventWide(List.of(rl))).isTrue();
    }

    // -------------------------------------------------------------------------
    // shouldDeleteEventWide — mehrere Ergebnislisten
    // -------------------------------------------------------------------------

    @Test
    void shouldDeleteEventWide_multipleResultListsSameDate_returnsTrue() {
        ZonedDateTime today = ZonedDateTime.now();
        ResultList rl1 = rlWithCreateTime(today.withHour(9));
        ResultList rl2 = rlWithCreateTime(today.withHour(14));

        assertThat(ResultListScoringService.shouldDeleteEventWide(List.of(rl1, rl2))).isTrue();
    }

    @Test
    void shouldDeleteEventWide_multipleResultListsDifferentDates_returnsFalse() {
        ZonedDateTime today = ZonedDateTime.now();
        ResultList rl1 = rlWithCreateTime(today);
        ResultList rl2 = rlWithCreateTime(today.minusDays(1));

        assertThat(ResultListScoringService.shouldDeleteEventWide(List.of(rl1, rl2))).isFalse();
    }

    @Test
    void shouldDeleteEventWide_multipleResultListsBothNullCreateTime_returnsTrue() {
        // Beide null → beide identisch → 1 distinkte Gruppe → gleicher Tag
        ResultList rl1 = rlWithCreateTime(null);
        ResultList rl2 = rlWithCreateTime(null);

        assertThat(ResultListScoringService.shouldDeleteEventWide(List.of(rl1, rl2))).isTrue();
    }

    @Test
    void shouldDeleteEventWide_multipleResultListsNullAndNonNullCreateTime_returnsFalse() {
        ResultList rl1 = rlWithCreateTime(null);
        ResultList rl2 = rlWithCreateTime(ZonedDateTime.now());

        assertThat(ResultListScoringService.shouldDeleteEventWide(List.of(rl1, rl2))).isFalse();
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    private static ResultList rlWithRaceNum(byte raceNum) {
        PersonRaceResult prr = PersonRaceResult.of("H21", 1L, null, null, 1000.0, 1L, raceNum, ResultStatus.OK);
        PersonResult pr = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of(prr));
        ClassResult cr = ClassResult.of("H21", "H21", null, List.of(pr), null);
        return new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.of(1L), null, null, null, List.of(cr));
    }

    private static ResultList rlWithNullRaceNum() {
        PersonRaceResult prr = PersonRaceResult.of("H21", 1L, null, null, 1000.0, 1L, null, ResultStatus.OK);
        PersonResult pr = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of(prr));
        ClassResult cr = ClassResult.of("H21", "H21", null, List.of(pr), null);
        return new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.of(1L), null, null, null, List.of(cr));
    }

    private static ResultList rlWithCreateTime(ZonedDateTime createTime) {
        return new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.of(1L), null, createTime, null, null);
    }

    private static Event aggregatedEvent() {
        Event event = Event.of("Test Event");
        event.setAggregatedScore(true);
        return event;
    }
}
