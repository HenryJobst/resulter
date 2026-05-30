package de.jobst.resulter.domain;

import de.jobst.resulter.domain.scoring.CupTypeCalculationStrategy;
import de.jobst.resulter.domain.scoring.KJCalculationStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassResultTest {

    @Mock
    CupTypeCalculationStrategy mockStrategy;

    @Captor
    ArgumentCaptor<List<PersonRaceResult>> personRaceResultsCaptor;

    @Captor
    ArgumentCaptor<Map<PersonId, OrganisationId>> orgByPersonCaptor;

    // -------------------------------------------------------------------------
    // of() — Fabrikmethode
    // -------------------------------------------------------------------------

    @Test
    void of_createsRecordWithAllFields() {
        ClassResult result = ClassResult.of("Herren 21", "H21", Gender.M, null, null);

        assertThat(result.classResultName().value()).isEqualTo("Herren 21");
        assertThat(result.classResultShortName().value()).isEqualTo("H21");
        assertThat(result.gender()).isEqualTo(Gender.M);
        assertThat(result.personResults().value()).isEmpty();
        assertThat(result.courseId()).isNull();
    }

    // -------------------------------------------------------------------------
    // compareTo — alphabetisch nach classResultName
    // -------------------------------------------------------------------------

    @Test
    void compareTo_ordersByClassResultNameAlphabetically() {
        ClassResult h21 = ClassResult.of("Herren 21", "H21", null, null, null);
        ClassResult d21 = ClassResult.of("Damen 21", "D21", null, null, null);

        assertThat(h21.compareTo(d21)).isGreaterThan(0);
        assertThat(d21.compareTo(h21)).isLessThan(0);
        assertThat(h21.compareTo(h21)).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // calculate — nicht-KJ-Zweig: nur OK-PersonRaceResults werden durchgereicht
    // -------------------------------------------------------------------------

    @Test
    void calculate_nonKjStrategy_passesOnlyOkPersonRaceResultsToStrategy() {
        Cup cup = Cup.of(1L, "NOR Cup", CupType.NOR, Year.of(2025), List.of());
        when(mockStrategy.valid(any(PersonResult.class))).thenReturn(true);
        when(mockStrategy.calculate(any(), anyList(), anyMap())).thenReturn(List.of());

        ClassResult classResult = ClassResult.of("Herren 21", "H21", null, List.of(
            personWithRaceResult(1L, 1L, "H21", ResultStatus.OK, 1000.0, 1L),
            personWithRaceResult(2L, 2L, "H21", ResultStatus.DID_NOT_FINISH, null, null)
        ), null);

        classResult.calculate(cup, mockStrategy);

        verify(mockStrategy).calculate(eq(cup), personRaceResultsCaptor.capture(), anyMap());
        assertThat(personRaceResultsCaptor.getValue()).hasSize(1);
        assertThat(personRaceResultsCaptor.getValue().getFirst().getPersonId()).isEqualTo(PersonId.of(1L));
    }

    @Test
    void calculate_nonKjStrategy_personResultsAreFilteredByStrategyValidMethod() {
        Cup cup = Cup.of(1L, "NOR Cup", CupType.NOR, Year.of(2025), List.of());
        // Nur Person 1 besteht den valid()-Filter der Strategie
        when(mockStrategy.valid(any(PersonResult.class))).thenAnswer(inv -> {
            PersonResult pr = inv.getArgument(0);
            return pr.personId().equals(PersonId.of(1L));
        });
        when(mockStrategy.calculate(any(), anyList(), anyMap())).thenReturn(List.of());

        ClassResult classResult = ClassResult.of("Herren 21", "H21", null, List.of(
            personWithRaceResult(1L, 1L, "H21", ResultStatus.OK, 1000.0, 1L),
            personWithRaceResult(2L, 2L, "H21", ResultStatus.OK, 1100.0, 2L)
        ), null);

        classResult.calculate(cup, mockStrategy);

        verify(mockStrategy).calculate(eq(cup), personRaceResultsCaptor.capture(), anyMap());
        assertThat(personRaceResultsCaptor.getValue()).hasSize(1);
        assertThat(personRaceResultsCaptor.getValue().getFirst().getPersonId()).isEqualTo(PersonId.of(1L));
    }

    @Test
    void calculate_nonKjStrategy_organisationMapExcludesPersonsWithNullOrgId() {
        Cup cup = Cup.of(1L, "NOR Cup", CupType.NOR, Year.of(2025), List.of());
        when(mockStrategy.valid(any(PersonResult.class))).thenReturn(true);
        when(mockStrategy.calculate(any(), anyList(), anyMap())).thenReturn(List.of());

        PersonRaceResult race1 = PersonRaceResult.of("H21", 1L, null, null, 1000.0, 1L, (byte) 1, ResultStatus.OK);
        PersonRaceResult race2 = PersonRaceResult.of("H21", 2L, null, null, 900.0, 1L, (byte) 1, ResultStatus.OK);
        PersonResult withOrg = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(1L), OrganisationId.of(10L), List.of(race1));
        PersonResult withoutOrg = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(2L), null, List.of(race2));

        ClassResult classResult = ClassResult.of("Herren 21", "H21", null, List.of(withOrg, withoutOrg), null);
        classResult.calculate(cup, mockStrategy);

        verify(mockStrategy).calculate(eq(cup), anyList(), orgByPersonCaptor.capture());
        assertThat(orgByPersonCaptor.getValue()).containsKey(PersonId.of(1L));
        assertThat(orgByPersonCaptor.getValue()).doesNotContainKey(PersonId.of(2L));
    }

    @Test
    void calculate_nonKjStrategy_returnsScoresFromStrategy() {
        Cup cup = Cup.of(1L, "NOR Cup", CupType.NOR, Year.of(2025), List.of());
        CupScore score = new CupScore(PersonId.of(1L), OrganisationId.of(1L), ClassResultShortName.of("H21"), 12.0);
        when(mockStrategy.valid(any(PersonResult.class))).thenReturn(true);
        when(mockStrategy.calculate(any(), anyList(), anyMap())).thenReturn(List.of(score));

        ClassResult classResult = ClassResult.of("Herren 21", "H21", null, List.of(
            personWithRaceResult(1L, 1L, "H21", ResultStatus.OK, 1000.0, 1L)
        ), null);

        List<CupScore> scores = classResult.calculate(cup, mockStrategy);

        assertThat(scores).containsExactly(score);
    }

    // -------------------------------------------------------------------------
    // calculate — KJ-Zweig: NOT_COMPETING/DID_NOT_START/DID_NOT_ENTER ausgeschlossen
    // -------------------------------------------------------------------------

    @Test
    void calculate_kjStrategy_excludesNotCompetingDidNotStartDidNotEnter() {
        // KJ(1) → Region(10) → Clubs [21,22,23,24,25]
        Map<OrganisationId, Organisation> orgMap = new HashMap<>();
        orgMap.put(OrganisationId.of(1L), Organisation.of(1L, "KJ", "KJ", "Other", null, List.of(OrganisationId.of(10L))));
        orgMap.put(OrganisationId.of(10L), Organisation.of(10L, "Region", "REG", "Other", null,
                List.of(OrganisationId.of(21L), OrganisationId.of(22L), OrganisationId.of(23L),
                        OrganisationId.of(24L), OrganisationId.of(25L))));
        KJCalculationStrategy strategy = new KJCalculationStrategy(orgMap);
        Cup cup = Cup.of(1L, "KJ Cup", CupType.KJ, Year.of(2025), List.of());

        ClassResult classResult = ClassResult.of("Herren 14", "H14", null, List.of(
            personWithRaceResult(1L, 21L, "H14", ResultStatus.OK, 1000.0, 1L),
            personWithRaceResult(2L, 22L, "H14", ResultStatus.NOT_COMPETING, null, null),
            personWithRaceResult(3L, 23L, "H14", ResultStatus.DID_NOT_FINISH, null, null),
            personWithRaceResult(4L, 24L, "H14", ResultStatus.DID_NOT_START, null, null),
            personWithRaceResult(5L, 25L, "H14", ResultStatus.DID_NOT_ENTER, null, null)
        ), null);

        List<CupScore> scores = classResult.calculate(cup, strategy);

        // OK (1) und DNF (3) erhalten Punkte; NOT_COMPETING (2), DID_NOT_START (4), DID_NOT_ENTER (5) werden herausgefiltert
        assertThat(scores.stream().map(s -> s.personId().value()).toList())
                .containsExactlyInAnyOrder(1L, 3L);
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------

    private static PersonResult personWithRaceResult(
            Long personId, Long orgId, String className, ResultStatus status, Double runtime, Long position) {
        PersonRaceResult raceResult =
                PersonRaceResult.of(className, personId, null, null, runtime, position, (byte) 1, status);
        return PersonResult.of(
                ClassResultShortName.of(className), PersonId.of(personId), OrganisationId.of(orgId),
                List.of(raceResult));
    }
}
