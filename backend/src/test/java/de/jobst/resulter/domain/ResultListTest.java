package de.jobst.resulter.domain;

import de.jobst.resulter.domain.scoring.CupTypeCalculationStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultListTest {

    @Mock
    CupTypeCalculationStrategy mockStrategy;

    // -------------------------------------------------------------------------
    // getDomainKey — Felder korrekt abgebildet
    // -------------------------------------------------------------------------

    @Test
    void getDomainKey_returnsCorrectFields() {
        ZonedDateTime createTime = ZonedDateTime.now();
        ResultList rl = new ResultList(
                ResultListId.of(1L), EventId.of(42L), RaceId.of(7L), "tester", createTime, "Active", null);

        ResultList.DomainKey key = rl.getDomainKey();

        assertThat(key.eventId()).isEqualTo(42L);
        assertThat(key.raceId()).isEqualTo(7L);
        assertThat(key.creator()).isEqualTo("tester");
        assertThat(key.createTime()).isEqualTo(createTime);
    }

    // -------------------------------------------------------------------------
    // getRaceNumber
    // -------------------------------------------------------------------------

    @Test
    void getRaceNumber_returnsEmptyWhenClassResultsIsNull() {
        ResultList rl = new ResultList(
                ResultListId.of(1L), EventId.of(1L), RaceId.of(1L), null, null, null, null);

        assertThat(rl.getRaceNumber()).isEqualTo(RaceNumber.empty());
    }

    @Test
    void getRaceNumber_returnsRaceNumberFromFirstPersonRaceResult() {
        PersonRaceResult raceResult =
                PersonRaceResult.of("H21", 1L, null, null, 1000.0, 1L, (byte) 3, ResultStatus.OK);
        PersonResult person = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of(raceResult));
        ClassResult classResult = ClassResult.of("Herren 21", "H21", null, List.of(person), null);

        ResultList rl = new ResultList(
                ResultListId.of(1L), EventId.of(1L), RaceId.of(1L), null, null, null, List.of(classResult));

        assertThat(rl.getRaceNumber()).isEqualTo(RaceNumber.of((byte) 3));
    }

    // -------------------------------------------------------------------------
    // calculate — ungültiger Cup (Event nicht in Cup-Liste)
    // -------------------------------------------------------------------------

    @Test
    void calculate_returnsNullWhenEventNotInCup() {
        Cup cup = Cup.of(1L, "Test Cup", CupType.NOR, Year.of(2025), List.of(EventId.of(99L)));
        ResultList rl = new ResultList(
                ResultListId.of(1L), EventId.of(1L), RaceId.of(1L), null, null, null, List.of());

        assertThat(rl.calculate(cup, "creator", ZonedDateTime.now(), mockStrategy)).isNull();
    }

    @Test
    void calculate_returnsNullWhenCupHasNoEvents() {
        Cup cup = Cup.of(1L, "Test Cup", CupType.NOR, Year.of(2025), List.of());
        ResultList rl = new ResultList(
                ResultListId.of(1L), EventId.of(1L), RaceId.of(1L), null, null, null, List.of());

        assertThat(rl.calculate(cup, "creator", ZonedDateTime.now(), mockStrategy)).isNull();
    }

    // -------------------------------------------------------------------------
    // calculate — gültiger Cup gibt CupScoreList zurück
    // -------------------------------------------------------------------------

    @Test
    void calculate_returnsCupScoreListWhenEventIsInCup() {
        Cup cup = Cup.of(1L, "Test Cup", CupType.NOR, Year.of(2025), List.of(EventId.of(1L)));
        ResultList rl = new ResultList(
                ResultListId.of(5L), EventId.of(1L), RaceId.of(2L), null, null, null, List.of());

        CupScoreList result = rl.calculate(cup, "creator", ZonedDateTime.now(), mockStrategy);

        assertThat(result).isNotNull();
        assertThat(result.getCupId()).isEqualTo(cup.getId());
        assertThat(result.getResultListId()).isEqualTo(ResultListId.of(5L));
        assertThat(result.getCupScores()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // compareTo — Sortierung: RaceNumber → createTime → raceId
    // -------------------------------------------------------------------------

    @Test
    void compareTo_ordersNullClassResultsByRaceId() {
        ResultList rl1 = new ResultList(
                ResultListId.of(1L), EventId.of(1L), RaceId.of(1L), null, null, null, null);
        ResultList rl2 = new ResultList(
                ResultListId.of(2L), EventId.of(1L), RaceId.of(2L), null, null, null, null);

        assertThat(rl1.compareTo(rl2)).isLessThan(0);
        assertThat(rl2.compareTo(rl1)).isGreaterThan(0);
        assertThat(rl1.compareTo(rl1)).isEqualTo(0);
    }

    @Test
    void compareTo_ordersNullClassResultsByCreateTimeBeforeRaceId() {
        ZonedDateTime earlier = ZonedDateTime.now().minusHours(1);
        ZonedDateTime later = ZonedDateTime.now();

        // Gleiche raceId, aber unterschiedliche createTime → createTime entscheidet
        ResultList rl1 = new ResultList(
                ResultListId.of(1L), EventId.of(1L), RaceId.of(5L), null, earlier, null, null);
        ResultList rl2 = new ResultList(
                ResultListId.of(2L), EventId.of(1L), RaceId.of(5L), null, later, null, null);

        assertThat(rl1.compareTo(rl2)).isLessThan(0);
        assertThat(rl2.compareTo(rl1)).isGreaterThan(0);
    }

    @Test
    void compareTo_ordersByRaceNumberWhenBothHaveClassResults() {
        ResultList rl1 = resultListWithRaceNumber(RaceId.of(1L), (byte) 1);
        ResultList rl2 = resultListWithRaceNumber(RaceId.of(2L), (byte) 2);

        assertThat(rl1.compareTo(rl2)).isLessThan(0);
        assertThat(rl2.compareTo(rl1)).isGreaterThan(0);
    }

    // -------------------------------------------------------------------------
    // getReferencedOrganisationIds
    // -------------------------------------------------------------------------

    @Test
    void getReferencedOrganisationIds_returnsEmptySetWhenClassResultsIsNull() {
        ResultList rl = new ResultList(
                ResultListId.of(1L), EventId.of(1L), RaceId.of(1L), null, null, null, null);

        assertThat(rl.getReferencedOrganisationIds()).isEmpty();
    }

    @Test
    void getReferencedOrganisationIds_returnsDistinctOrgIdsAndIgnoresNullOrgs() {
        PersonResult p1 = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(1L), OrganisationId.of(10L), List.of());
        PersonResult p2 = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(2L), OrganisationId.of(20L), List.of());
        PersonResult p3 = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(3L), OrganisationId.of(10L), List.of()); // selbe Org wie p1
        PersonResult p4 = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(4L), null, List.of()); // keine Org → ignoriert

        ClassResult classResult = ClassResult.of("Herren 21", "H21", null, List.of(p1, p2, p3, p4), null);
        ResultList rl = new ResultList(
                ResultListId.of(1L), EventId.of(1L), RaceId.of(1L), null, null, null, List.of(classResult));

        Set<OrganisationId> orgIds = rl.getReferencedOrganisationIds();

        assertThat(orgIds).containsExactlyInAnyOrder(OrganisationId.of(10L), OrganisationId.of(20L));
    }

    @Test
    void getReferencedOrganisationIds_returnsEmptySetWhenNoPersonResultsHaveOrgs() {
        PersonResult p = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of());
        ClassResult classResult = ClassResult.of("Herren 21", "H21", null, List.of(p), null);
        ResultList rl = new ResultList(
                ResultListId.of(1L), EventId.of(1L), RaceId.of(1L), null, null, null, List.of(classResult));

        assertThat(rl.getReferencedOrganisationIds()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // calculate — mit echten ClassResults und konfiguriertem Mock
    // -------------------------------------------------------------------------

    @Test
    void calculate_withMultipleClassResultsHarmonizedToSameName_mergesPersonResults() {
        // Zwei Klassen "D21" und "D21L" werden auf denselben harmonisierten Namen "D19L" abgebildet
        ClassResultShortName harmonized = ClassResultShortName.of("D19L");

        PersonRaceResult rr1 = PersonRaceResult.of("D21", 1L, null, null, 1000.0, 1L, (byte) 1, ResultStatus.OK);
        PersonResult p1 = PersonResult.of(ClassResultShortName.of("D21"), PersonId.of(1L), OrganisationId.of(1L), List.of(rr1));
        ClassResult cr1 = ClassResult.of("Damen 21", "D21", null, List.of(p1), null);

        PersonRaceResult rr2 = PersonRaceResult.of("D21L", 2L, null, null, 1100.0, 2L, (byte) 1, ResultStatus.OK);
        PersonResult p2 = PersonResult.of(ClassResultShortName.of("D21L"), PersonId.of(2L), OrganisationId.of(2L), List.of(rr2));
        ClassResult cr2 = ClassResult.of("Damen 21 Lang", "D21L", null, List.of(p2), null);

        Cup cup = Cup.of(1L, "NOR Cup", CupType.NOR, Year.of(2025), List.of(EventId.of(1L)));
        ResultList rl = new ResultList(
                ResultListId.of(5L), EventId.of(1L), RaceId.of(1L), null, null, null, List.of(cr1, cr2));

        // Beide Klassen harmonisieren auf "D19L"
        when(mockStrategy.harmonizeClassResultShortName(any())).thenReturn(harmonized);
        when(mockStrategy.valid(any(ClassResult.class))).thenReturn(true);
        when(mockStrategy.valid(any(PersonResult.class))).thenReturn(true);
        when(mockStrategy.calculate(any(), any(), any())).thenReturn(List.of(
                CupScore.of(PersonId.of(1L), OrganisationId.of(1L), harmonized, 12.0),
                CupScore.of(PersonId.of(2L), OrganisationId.of(2L), harmonized, 11.0)));

        CupScoreList result = rl.calculate(cup, "creator", ZonedDateTime.now(), mockStrategy);

        assertThat(result).isNotNull();
        assertThat(result.getCupScores()).hasSize(2);
    }

    @Test
    void calculate_withClassResults_invokesCupTypeCalculation() {
        // Arrange
        PersonRaceResult raceResult = PersonRaceResult.of("H21", 1L, null, null, 1000.0, 1L, (byte) 1, ResultStatus.OK);
        PersonResult person = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(1L), OrganisationId.of(10L), List.of(raceResult));
        ClassResult classResult = ClassResult.of("Herren 21", "H21", null, List.of(person), null);

        Cup cup = Cup.of(1L, "Test Cup", CupType.NOR, Year.of(2025), List.of(EventId.of(1L)));
        ResultList rl = new ResultList(
                ResultListId.of(5L), EventId.of(1L), RaceId.of(1L), null, null, null, List.of(classResult));

        CupScore score = CupScore.of(PersonId.of(1L), OrganisationId.of(10L), ClassResultShortName.of("H21"), 12.0);

        // harmonizeClassResultShortName gibt den Namen unverändert zurück
        when(mockStrategy.harmonizeClassResultShortName(any())).thenAnswer(inv -> inv.getArgument(0));
        // valid(ClassResult) akzeptiert alle Klassen
        when(mockStrategy.valid(any(ClassResult.class))).thenReturn(true);
        // valid(PersonResult) akzeptiert alle Personen
        when(mockStrategy.valid(any(PersonResult.class))).thenReturn(true);
        // calculate gibt einen Score zurück
        when(mockStrategy.calculate(any(), any(), any())).thenReturn(List.of(score));

        // Act
        CupScoreList result = rl.calculate(cup, "creator", ZonedDateTime.now(), mockStrategy);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCupScores()).hasSize(1);
        assertThat(result.getCupScores().getFirst().score()).isEqualTo(12.0);
    }

    // -------------------------------------------------------------------------
    // compareTo — Fallthrough durch gleiche RaceNumber
    // -------------------------------------------------------------------------

    @Test
    void compareTo_sameRaceNumber_ordersByCreateTime() {
        ZonedDateTime earlier = ZonedDateTime.now().minusHours(1);
        ZonedDateTime later = ZonedDateTime.now();

        ResultList rl1 = resultListWithRaceNumberAndCreateTime((byte) 1, earlier);
        ResultList rl2 = resultListWithRaceNumberAndCreateTime((byte) 1, later);

        assertThat(rl1.compareTo(rl2)).isLessThan(0);
        assertThat(rl2.compareTo(rl1)).isGreaterThan(0);
    }

    @Test
    void compareTo_sameRaceNumberAndCreateTime_ordersByRaceId() {
        ZonedDateTime now = ZonedDateTime.now();

        ResultList rl1 = resultListWithRaceNumberCreateTimeAndRaceId((byte) 1, now, RaceId.of(1L));
        ResultList rl2 = resultListWithRaceNumberCreateTimeAndRaceId((byte) 1, now, RaceId.of(2L));

        assertThat(rl1.compareTo(rl2)).isLessThan(0);
        assertThat(rl2.compareTo(rl1)).isGreaterThan(0);
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    private static ResultList resultListWithRaceNumber(RaceId raceId, byte raceNumber) {
        PersonRaceResult raceResult =
                PersonRaceResult.of("H21", 1L, null, null, 1000.0, 1L, raceNumber, ResultStatus.OK);
        PersonResult person = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of(raceResult));
        ClassResult classResult = ClassResult.of("Herren 21", "H21", null, List.of(person), null);
        return new ResultList(
                ResultListId.of(1L), EventId.of(1L), raceId, null, null, null, List.of(classResult));
    }

    private static ResultList resultListWithRaceNumberAndCreateTime(byte raceNumber, ZonedDateTime createTime) {
        PersonRaceResult raceResult =
                PersonRaceResult.of("H21", 1L, null, null, 1000.0, 1L, raceNumber, ResultStatus.OK);
        PersonResult person = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of(raceResult));
        ClassResult classResult = ClassResult.of("Herren 21", "H21", null, List.of(person), null);
        return new ResultList(
                ResultListId.of(1L), EventId.of(1L), RaceId.of(1L), null, createTime, null, List.of(classResult));
    }

    private static ResultList resultListWithRaceNumberCreateTimeAndRaceId(byte raceNumber, ZonedDateTime createTime, RaceId raceId) {
        PersonRaceResult raceResult =
                PersonRaceResult.of("H21", 1L, null, null, 1000.0, 1L, raceNumber, ResultStatus.OK);
        PersonResult person = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of(raceResult));
        ClassResult classResult = ClassResult.of("Herren 21", "H21", null, List.of(person), null);
        return new ResultList(
                ResultListId.of(1L), EventId.of(1L), raceId, null, createTime, null, List.of(classResult));
    }
}
