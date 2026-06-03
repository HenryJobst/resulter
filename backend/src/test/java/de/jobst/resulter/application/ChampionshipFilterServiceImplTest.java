package de.jobst.resulter.application;

import de.jobst.resulter.application.port.OrganisationRepository;
import de.jobst.resulter.application.port.RaceRepository;
import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.springapp.config.SpringSecurityAuditorAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChampionshipFilterServiceImplTest {

    private ResultListRepository resultListRepository;
    private OrganisationRepository organisationRepository;
    private RaceRepository raceRepository;
    private SpringSecurityAuditorAware auditorAware;
    private ChampionshipFilterServiceImpl service;

    // org IDs
    private static final long BASE_ORG_ID = 10L;
    private static final long ELIGIBLE_CLUB_ID = 11L;
    private static final long NON_ELIGIBLE_CLUB_ID = 99L;

    // person IDs
    private static final long ELIGIBLE_PERSON_ID = 1L;
    private static final long NON_ELIGIBLE_PERSON_ID = 2L;

    private EventId eventId;
    private OrganisationId baseOrgId;
    private OrganisationId eligibleClubId;
    private OrganisationId nonEligibleClubId;

    @BeforeEach
    void setUp() {
        resultListRepository = mock(ResultListRepository.class);
        organisationRepository = mock(OrganisationRepository.class);
        raceRepository = mock(RaceRepository.class);
        auditorAware = mock(SpringSecurityAuditorAware.class);
        service = new ChampionshipFilterServiceImpl(
                resultListRepository, organisationRepository, raceRepository, auditorAware);

        eventId = EventId.of(1L);
        baseOrgId = OrganisationId.of(BASE_ORG_ID);
        eligibleClubId = OrganisationId.of(ELIGIBLE_CLUB_ID);
        nonEligibleClubId = OrganisationId.of(NON_ELIGIBLE_CLUB_ID);

        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of(SpringSecurityAuditorAware.UNKNOWN));
    }

    /**
     * Build an org tree where baseOrg (id=10) has child eligibleClub (id=11).
     * nonEligibleClub (id=99) is NOT in the tree.
     */
    private Map<OrganisationId, Organisation> orgTree() {
        Organisation eligibleClub = new Organisation(
                eligibleClubId,
                OrganisationName.of("EligibleClub"),
                OrganisationShortName.of("EC"),
                OrganisationType.CLUB,
                null,
                Collections.emptyList());

        Organisation baseOrg = new Organisation(
                baseOrgId,
                OrganisationName.of("BaseOrg"),
                OrganisationShortName.of("BO"),
                OrganisationType.NATIONAL_FEDERATION,
                null,
                List.of(eligibleClubId));

        Map<OrganisationId, Organisation> tree = new HashMap<>();
        tree.put(baseOrgId, baseOrg);
        tree.put(eligibleClubId, eligibleClub);
        return tree;
    }

    private PersonRaceResult makeRaceResult(String className, long personId, double runtime, ResultStatus status, byte raceNum) {
        return new PersonRaceResult(
                ClassResultShortName.of(className),
                PersonId.of(personId),
                DateTime.empty(),
                DateTime.empty(),
                PunchTime.of(runtime),
                Position.of(1L),
                status,
                RaceNumber.of(raceNum),
                null);
    }

    private ResultList makeResultList(long personId, OrganisationId orgId, ResultStatus status, byte raceNum) {
        PersonRaceResult raceResult = makeRaceResult("M21", personId, 1000.0, status, raceNum);
        PersonResult personResult = PersonResult.of(
                ClassResultShortName.of("M21"),
                PersonId.of(personId),
                orgId,
                List.of(raceResult));
        ClassResult classResult = ClassResult.of("M21", "M21", Gender.M, List.of(personResult), null);
        ResultList resultList = new ResultList(
                ResultListId.of(1L),
                eventId,
                RaceId.of(1L),
                "test",
                null,
                null,
                List.of(classResult));
        return resultList;
    }

    // ===================== Cleanup Tests =====================

    @Test
    void cleanup_eligibleParticipant_remainsUnchanged() {
        ResultList resultList = makeResultList(ELIGIBLE_PERSON_ID, eligibleClubId, ResultStatus.OK, (byte) 1);

        Map<OrganisationId, Organisation> tree = orgTree();
        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(resultList));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(tree);

        service.applyChampionshipCleanup(eventId, baseOrgId, Set.of());

        // Capture what was saved
        verify(resultListRepository).update(resultList);
        // The eligible person's race result state should remain OK
        ResultStatus state = resultList.getClassResults().stream()
                .flatMap(cr -> cr.personResults().value().stream())
                .flatMap(pr -> pr.personRaceResults().value().stream())
                .findFirst()
                .orElseThrow()
                .getState();
        assertThat(state).isEqualTo(ResultStatus.OK);
    }

    @Test
    void cleanup_nonEligibleParticipant_markedAsNotCompeting() {
        ResultList resultList = makeResultList(NON_ELIGIBLE_PERSON_ID, nonEligibleClubId, ResultStatus.OK, (byte) 1);

        Map<OrganisationId, Organisation> tree = orgTree();
        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(resultList));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(tree);

        service.applyChampionshipCleanup(eventId, baseOrgId, Set.of());

        verify(resultListRepository).update(resultList);
        ResultStatus state = resultList.getClassResults().stream()
                .flatMap(cr -> cr.personResults().value().stream())
                .flatMap(pr -> pr.personRaceResults().value().stream())
                .findFirst()
                .orElseThrow()
                .getState();
        assertThat(state).isEqualTo(ResultStatus.NOT_COMPETING);
    }

    @Test
    void cleanup_nullOrganisation_markedAsNotCompeting() {
        // Person with null organisationId
        PersonRaceResult raceResult = makeRaceResult("M21", ELIGIBLE_PERSON_ID, 1000.0, ResultStatus.OK, (byte) 1);
        PersonResult personResult = PersonResult.of(
                ClassResultShortName.of("M21"),
                PersonId.of(ELIGIBLE_PERSON_ID),
                null, // null organisation
                List.of(raceResult));
        ClassResult classResult = ClassResult.of("M21", "M21", Gender.M, List.of(personResult), null);
        ResultList resultList = new ResultList(
                ResultListId.of(1L), eventId, RaceId.of(1L), "test", null, null, List.of(classResult));

        Map<OrganisationId, Organisation> tree = orgTree();
        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(resultList));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(tree);

        service.applyChampionshipCleanup(eventId, baseOrgId, Set.of());

        verify(resultListRepository).update(resultList);
        ResultStatus state = resultList.getClassResults().stream()
                .flatMap(cr -> cr.personResults().value().stream())
                .flatMap(pr -> pr.personRaceResults().value().stream())
                .findFirst()
                .orElseThrow()
                .getState();
        assertThat(state).isEqualTo(ResultStatus.NOT_COMPETING);
    }

    // ===================== Ranking Tests =====================

    @Test
    void ranking_eligibleFirst_nonEligibleLast() {
        // Non-eligible has faster time (900), eligible has slower time (1000)
        PersonRaceResult eligibleRaceResult = makeRaceResult("M21", ELIGIBLE_PERSON_ID, 1000.0, ResultStatus.OK, (byte) 1);
        PersonResult eligiblePerson = PersonResult.of(
                ClassResultShortName.of("M21"), PersonId.of(ELIGIBLE_PERSON_ID), eligibleClubId, List.of(eligibleRaceResult));

        PersonRaceResult nonEligibleRaceResult = makeRaceResult("M21", NON_ELIGIBLE_PERSON_ID, 900.0, ResultStatus.OK, (byte) 1);
        PersonResult nonEligiblePerson = PersonResult.of(
                ClassResultShortName.of("M21"), PersonId.of(NON_ELIGIBLE_PERSON_ID), nonEligibleClubId, List.of(nonEligibleRaceResult));

        ClassResult classResult = ClassResult.of("M21", "M21", Gender.M, List.of(eligiblePerson, nonEligiblePerson), null);
        ResultList sourceResultList = new ResultList(
                ResultListId.of(1L), eventId, RaceId.of(1L), "test", null, null, List.of(classResult));

        Map<OrganisationId, Organisation> tree = orgTree();
        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(sourceResultList));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(tree);

        Race race0 = Race.of(RaceId.of(100L), eventId, null, (byte) 0);
        when(raceRepository.findOrCreate(any(Race.class))).thenReturn(race0);

        ResultList savedResultList = new ResultList(
                ResultListId.of(2L), eventId, race0.getId(), "test", null, null, null);
        when(resultListRepository.save(any(ResultList.class))).thenReturn(savedResultList);

        List<ResultList> result = service.addChampionshipRanking(eventId, baseOrgId, Set.of());

        assertThat(result).hasSize(1);
        verify(resultListRepository).save(any(ResultList.class));

        // Inspect the argument passed to save
        org.mockito.ArgumentCaptor<ResultList> captor = org.mockito.ArgumentCaptor.forClass(ResultList.class);
        verify(resultListRepository).save(captor.capture());
        ResultList createdList = captor.getValue();

        List<PersonResult> personResults = createdList.getClassResults().stream()
                .flatMap(cr -> cr.personResults().value().stream())
                .toList();

        // Eligible person should be at position 1 with state OK
        PersonResult eligibleResult = personResults.stream()
                .filter(pr -> pr.personId().equals(PersonId.of(ELIGIBLE_PERSON_ID)))
                .findFirst().orElseThrow();
        PersonRaceResult eligibleRace = eligibleResult.personRaceResults().value().stream().findFirst().orElseThrow();
        assertThat(eligibleRace.getState()).isEqualTo(ResultStatus.OK);
        assertThat(eligibleRace.getPosition().value()).isEqualTo(1L);
        assertThat(eligibleRace.getRaceNumber().value()).isEqualTo((byte) 0);

        // Non-eligible person should be at position 2 with state NOT_COMPETING
        PersonResult nonEligibleResult = personResults.stream()
                .filter(pr -> pr.personId().equals(PersonId.of(NON_ELIGIBLE_PERSON_ID)))
                .findFirst().orElseThrow();
        PersonRaceResult nonEligibleRace = nonEligibleResult.personRaceResults().value().stream().findFirst().orElseThrow();
        assertThat(nonEligibleRace.getState()).isEqualTo(ResultStatus.NOT_COMPETING);
        assertThat(nonEligibleRace.getPosition().value()).isEqualTo(2L);
    }

    @Test
    void ranking_eligibleSortedByRuntime() {
        long fasterPersonId = 3L;
        long slowerPersonId = 4L;

        PersonRaceResult fasterRaceResult = makeRaceResult("M21", fasterPersonId, 900.0, ResultStatus.OK, (byte) 1);
        PersonResult fasterPerson = PersonResult.of(
                ClassResultShortName.of("M21"), PersonId.of(fasterPersonId), eligibleClubId, List.of(fasterRaceResult));

        PersonRaceResult slowerRaceResult = makeRaceResult("M21", slowerPersonId, 1200.0, ResultStatus.OK, (byte) 1);
        PersonResult slowerPerson = PersonResult.of(
                ClassResultShortName.of("M21"), PersonId.of(slowerPersonId), eligibleClubId, List.of(slowerRaceResult));

        ClassResult classResult = ClassResult.of("M21", "M21", Gender.M, List.of(slowerPerson, fasterPerson), null);
        ResultList sourceResultList = new ResultList(
                ResultListId.of(1L), eventId, RaceId.of(1L), "test", null, null, List.of(classResult));

        Map<OrganisationId, Organisation> tree = orgTree();
        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(sourceResultList));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(tree);

        Race race0 = Race.of(RaceId.of(100L), eventId, null, (byte) 0);
        when(raceRepository.findOrCreate(any(Race.class))).thenReturn(race0);

        ResultList savedResultList = new ResultList(
                ResultListId.of(2L), eventId, race0.getId(), "test", null, null, null);
        when(resultListRepository.save(any(ResultList.class))).thenReturn(savedResultList);

        service.addChampionshipRanking(eventId, baseOrgId, Set.of());

        org.mockito.ArgumentCaptor<ResultList> captor = org.mockito.ArgumentCaptor.forClass(ResultList.class);
        verify(resultListRepository).save(captor.capture());
        ResultList createdList = captor.getValue();

        List<PersonResult> personResults = createdList.getClassResults().stream()
                .flatMap(cr -> cr.personResults().value().stream())
                .toList();

        PersonResult fasterResult = personResults.stream()
                .filter(pr -> pr.personId().equals(PersonId.of(fasterPersonId)))
                .findFirst().orElseThrow();
        PersonRaceResult fasterRace = fasterResult.personRaceResults().value().stream().findFirst().orElseThrow();
        assertThat(fasterRace.getPosition().value()).isEqualTo(1L);

        PersonResult slowerResult = personResults.stream()
                .filter(pr -> pr.personId().equals(PersonId.of(slowerPersonId)))
                .findFirst().orElseThrow();
        PersonRaceResult slowerRace = slowerResult.personRaceResults().value().stream().findFirst().orElseThrow();
        assertThat(slowerRace.getPosition().value()).isEqualTo(2L);
    }

    @Test
    void ranking_existingRace0_isExcludedFromSources() {
        // Setup: one race-1 source list and one existing race-0 list
        PersonRaceResult race1Prr = makeRaceResult("H35", ELIGIBLE_PERSON_ID, 1000.0, ResultStatus.OK, (byte) 1);
        PersonResult eligibleInRace1 = PersonResult.of(
                ClassResultShortName.of("H35"), PersonId.of(ELIGIBLE_PERSON_ID), eligibleClubId, List.of(race1Prr));
        ClassResult race1ClassResult = ClassResult.of("H35 (M35-)", "H35", Gender.M, List.of(eligibleInRace1), null);
        ResultList sourceList = new ResultList(
                ResultListId.of(1L), eventId, RaceId.of(1L), "test", null, null, List.of(race1ClassResult));

        // Existing race-0 list: contains a PersonRaceResult with raceNumber=0
        PersonRaceResult race0Prr = new PersonRaceResult(
                ClassResultShortName.of("H35"),
                PersonId.of(ELIGIBLE_PERSON_ID),
                DateTime.empty(),
                DateTime.empty(),
                PunchTime.of(1000.0),
                Position.of(1L),
                ResultStatus.OK,
                RaceNumber.of((byte) 0),
                null);
        PersonResult existingRace0Person = PersonResult.of(
                ClassResultShortName.of("H35"), PersonId.of(ELIGIBLE_PERSON_ID), eligibleClubId, List.of(race0Prr));
        ClassResult existingRace0ClassResult = ClassResult.of(
                "H35 (M35-)", "H35", Gender.M, List.of(existingRace0Person), null);
        ResultList existingRace0List = new ResultList(
                ResultListId.of(2L),
                eventId,
                RaceId.of(99L),
                "test",
                null,
                "active",
                List.of(existingRace0ClassResult));

        Race race0 = Race.of(eventId, (byte) 0);
        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(sourceList, existingRace0List));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(orgTree());
        when(raceRepository.findOrCreate(any(Race.class))).thenReturn(race0);
        when(auditorAware.getCurrentAuditor()).thenReturn(Optional.of("test"));
        when(resultListRepository.save(any(ResultList.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ResultList> created = service.addChampionshipRanking(eventId, baseOrgId, Set.of());

        // Should create exactly one new race-0 list with only the source (race-1) data
        assertThat(created).hasSize(1);
        ClassResult cr = created.getFirst().getClassResults().iterator().next();
        assertThat(cr.personResults().value()).hasSize(1); // only the eligible person from race-1
    }

    @Test
    void ranking_emptySourceResultLists_returnsEmpty() {
        when(resultListRepository.findByEventId(eventId)).thenReturn(Collections.emptyList());
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(orgTree());

        List<ResultList> result = service.addChampionshipRanking(eventId, baseOrgId, Set.of());

        assertThat(result).isEmpty();
        verify(resultListRepository, never()).findOrCreate(any(ResultList.class));
    }

    @Test
    void findClassShortNames_delegatesToRepository() {
        when(resultListRepository.findClassShortNamesByEventId(eventId)).thenReturn(List.of("H21", "D21"));
        Set<String> result = service.findClassShortNames(eventId);
        assertThat(result).containsExactlyInAnyOrder("H21", "D21");
    }

    @Test
    void hasMultipleRaces_returnsTrueWhenCountGreaterThanOne() {
        when(resultListRepository.countNonZeroRacesByEventId(eventId)).thenReturn(2);
        assertThat(service.hasMultipleRaces(eventId)).isTrue();
    }

    @Test
    void hasMultipleRaces_returnsFalseWhenCountIsOne() {
        when(resultListRepository.countNonZeroRacesByEventId(eventId)).thenReturn(1);
        assertThat(service.hasMultipleRaces(eventId)).isFalse();
    }

    @Test
    void cleanup_withNullClassResults_skipsResultList() {
        ResultList resultList = new ResultList(
                ResultListId.of(1L), eventId, RaceId.of(1L), "test", null, null, null);
        Map<OrganisationId, Organisation> tree = orgTree();
        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(resultList));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(tree);

        service.applyChampionshipCleanup(eventId, baseOrgId, Set.of());

        verify(resultListRepository).update(resultList);
        assertThat(resultList.getClassResults()).isNull();
    }

    @Test
    void cleanup_nonEligibleWithNonOkStatus_keepsOriginalStatus() {
        PersonRaceResult raceResult = makeRaceResult("M21", NON_ELIGIBLE_PERSON_ID, 1000.0, ResultStatus.DID_NOT_FINISH, (byte) 1);
        PersonResult personResult = PersonResult.of(
                ClassResultShortName.of("M21"), PersonId.of(NON_ELIGIBLE_PERSON_ID), nonEligibleClubId, List.of(raceResult));
        ClassResult classResult = ClassResult.of("M21", "M21", Gender.M, List.of(personResult), null);
        ResultList resultList = new ResultList(
                ResultListId.of(1L), eventId, RaceId.of(1L), "test", null, null, List.of(classResult));

        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(resultList));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(orgTree());

        service.applyChampionshipCleanup(eventId, baseOrgId, Set.of());

        ResultStatus state = resultList.getClassResults().stream()
                .flatMap(cr -> cr.personResults().value().stream())
                .flatMap(pr -> pr.personRaceResults().value().stream())
                .findFirst().orElseThrow().getState();
        assertThat(state).isEqualTo(ResultStatus.DID_NOT_FINISH);
    }

    @Test
    void cleanup_excludedClass_isSkipped() {
        ResultList resultList = makeResultList(ELIGIBLE_PERSON_ID, eligibleClubId, ResultStatus.OK, (byte) 1);
        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(resultList));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(orgTree());

        service.applyChampionshipCleanup(eventId, baseOrgId, Set.of("M21"));

        // The class "M21" is excluded so updatedClassResults should be empty
        verify(resultListRepository).update(resultList);
        assertThat(resultList.getClassResults()).isEmpty();
    }

    @Test
    void ranking_excludedClass_isNotIncluded() {
        ResultList sourceResultList = makeResultList(ELIGIBLE_PERSON_ID, eligibleClubId, ResultStatus.OK, (byte) 1);
        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(sourceResultList));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(orgTree());
        Race race0 = Race.of(RaceId.of(100L), eventId, null, (byte) 0);
        when(raceRepository.findOrCreate(any(Race.class))).thenReturn(race0);
        when(resultListRepository.save(any(ResultList.class))).thenAnswer(inv -> inv.getArgument(0));

        // Exclude the only class
        List<ResultList> result = service.addChampionshipRanking(eventId, baseOrgId, Set.of("M21"));

        assertThat(result).hasSize(1);
        ResultList created = result.getFirst();
        assertThat(created.getClassResults()).isEmpty();
    }

    @Test
    void ranking_personWithoutOkStatus_goesToNonEligibleWithoutTime() {
        PersonRaceResult raceResult = makeRaceResult("M21", ELIGIBLE_PERSON_ID, Double.MAX_VALUE, ResultStatus.DID_NOT_FINISH, (byte) 1);
        PersonResult eligiblePerson = PersonResult.of(
                ClassResultShortName.of("M21"), PersonId.of(ELIGIBLE_PERSON_ID), eligibleClubId,
                List.of(new PersonRaceResult(
                        ClassResultShortName.of("M21"), PersonId.of(ELIGIBLE_PERSON_ID),
                        DateTime.empty(), DateTime.empty(), PunchTime.of((Double) null),
                        Position.of(1L), ResultStatus.DID_NOT_FINISH, RaceNumber.of((byte) 1), null)));
        ClassResult classResult = ClassResult.of("M21", "M21", Gender.M, List.of(eligiblePerson), null);
        ResultList sourceResultList = new ResultList(
                ResultListId.of(1L), eventId, RaceId.of(1L), "test", null, null, List.of(classResult));

        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(sourceResultList));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(orgTree());
        Race race0 = Race.of(RaceId.of(100L), eventId, null, (byte) 0);
        when(raceRepository.findOrCreate(any(Race.class))).thenReturn(race0);
        when(resultListRepository.save(any(ResultList.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ResultList> result = service.addChampionshipRanking(eventId, baseOrgId, Set.of());

        assertThat(result).hasSize(1);
        // The person should have DID_NOT_FINISH preserved (bestSourceStatus)
        PersonRaceResult prr = result.getFirst().getClassResults().stream().findFirst().orElseThrow()
                .personResults().value().stream().findFirst().orElseThrow()
                .personRaceResults().value().stream().findFirst().orElseThrow();
        assertThat(prr.getState()).isEqualTo(ResultStatus.DID_NOT_FINISH);
    }

    @Test
    void cleanup_baseOrgNotInTree_throwsIllegalArgumentException() {
        ResultList resultList = makeResultList(ELIGIBLE_PERSON_ID, eligibleClubId, ResultStatus.OK, (byte) 1);
        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(resultList));
        // Return a tree that does NOT contain baseOrgId
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(Map.of());

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> service.applyChampionshipCleanup(eventId, baseOrgId, Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.valueOf(BASE_ORG_ID));
    }

    @Test
    void ranking_isRace0_withEmptyPersonRaceResults_returnsFalse() {
        // A result list whose class has no PersonRaceResults → getRaceNumber() throws NoSuchElementException
        // isRace0 should catch that and return false (treated as non-race-0 source)
        ClassResult emptyClassResult = ClassResult.of("H21", "H21", Gender.M, List.of(), null);
        ResultList tricky = new ResultList(
                ResultListId.of(5L), eventId, RaceId.of(1L), "test", null, null, List.of(emptyClassResult));

        PersonRaceResult source = makeRaceResult("H21", ELIGIBLE_PERSON_ID, 500.0, ResultStatus.OK, (byte) 1);
        PersonResult sourcePerson = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(ELIGIBLE_PERSON_ID), eligibleClubId, List.of(source));
        ClassResult sourceCr = ClassResult.of("H21", "H21", Gender.M, List.of(sourcePerson), null);
        ResultList sourceList = new ResultList(
                ResultListId.of(6L), eventId, RaceId.of(1L), "test", null, null, List.of(sourceCr));

        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(tricky, sourceList));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(orgTree());
        Race race0 = Race.of(RaceId.of(100L), eventId, null, (byte) 0);
        when(raceRepository.findOrCreate(any(Race.class))).thenReturn(race0);
        when(resultListRepository.save(any(ResultList.class))).thenAnswer(inv -> inv.getArgument(0));

        // Should not throw and should process sourceList as a source
        List<ResultList> result = service.addChampionshipRanking(eventId, baseOrgId, Set.of());
        assertThat(result).hasSize(1);
    }

    @Test
    void ranking_tiedRuntimes_getOlympicRanking() {
        // Two eligible runners with the same runtime → both get position 1
        long personId1 = 10L, personId2 = 11L;
        PersonRaceResult prr1 = makeRaceResult("H21", personId1, 600.0, ResultStatus.OK, (byte) 1);
        PersonResult p1 = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(personId1), eligibleClubId, List.of(prr1));
        PersonRaceResult prr2 = makeRaceResult("H21", personId2, 600.0, ResultStatus.OK, (byte) 1);
        PersonResult p2 = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(personId2), eligibleClubId, List.of(prr2));

        ClassResult cr = ClassResult.of("H21", "H21", Gender.M, List.of(p1, p2), null);
        ResultList source = new ResultList(
                ResultListId.of(1L), eventId, RaceId.of(1L), "test", null, null, List.of(cr));

        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(source));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(orgTree());
        Race race0 = Race.of(RaceId.of(100L), eventId, null, (byte) 0);
        when(raceRepository.findOrCreate(any(Race.class))).thenReturn(race0);
        when(resultListRepository.save(any(ResultList.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ResultList> result = service.addChampionshipRanking(eventId, baseOrgId, Set.of());

        assertThat(result).hasSize(1);
        List<Long> positions = result.getFirst().getClassResults().stream()
                .flatMap(c -> c.personResults().value().stream())
                .flatMap(p -> p.personRaceResults().value().stream())
                .map(r -> r.getPosition().value())
                .toList();
        // Both runners have equal time → both should be at position 1
        assertThat(positions).containsOnly(1L);
    }

    @Test
    void ranking_sourceWithNullClassResults_isSkipped() {
        ResultList nullClassResultsList = new ResultList(
                ResultListId.of(1L), eventId, RaceId.of(1L), "test", null, null, null);
        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(nullClassResultsList));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(orgTree());
        Race race0 = Race.of(RaceId.of(100L), eventId, null, (byte) 0);
        when(raceRepository.findOrCreate(any(Race.class))).thenReturn(race0);
        when(resultListRepository.save(any(ResultList.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ResultList> result = service.addChampionshipRanking(eventId, baseOrgId, Set.of());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getClassResults()).isEmpty();
    }

    @Test
    void ranking_nonEligibleWithoutValidTime_preservesOriginalStatus() {
        PersonRaceResult raceResult = new PersonRaceResult(
                ClassResultShortName.of("M21"), PersonId.of(NON_ELIGIBLE_PERSON_ID),
                DateTime.empty(), DateTime.empty(),
                PunchTime.of((Double) null),
                Position.of(1L), ResultStatus.DID_NOT_FINISH,
                RaceNumber.of((byte) 1), null);
        PersonResult nonEligiblePerson = PersonResult.of(
                ClassResultShortName.of("M21"), PersonId.of(NON_ELIGIBLE_PERSON_ID),
                nonEligibleClubId, List.of(raceResult));
        ClassResult classResult = ClassResult.of("M21", "M21", Gender.M, List.of(nonEligiblePerson), null);
        ResultList sourceResultList = new ResultList(
                ResultListId.of(1L), eventId, RaceId.of(1L), "test", null, null, List.of(classResult));

        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(sourceResultList));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(orgTree());
        Race race0 = Race.of(RaceId.of(100L), eventId, null, (byte) 0);
        when(raceRepository.findOrCreate(any(Race.class))).thenReturn(race0);
        when(resultListRepository.save(any(ResultList.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ResultList> result = service.addChampionshipRanking(eventId, baseOrgId, Set.of());

        assertThat(result).hasSize(1);
        PersonRaceResult prr = result.getFirst().getClassResults().stream().findFirst().orElseThrow()
                .personResults().value().stream().findFirst().orElseThrow()
                .personRaceResults().value().stream().findFirst().orElseThrow();
        assertThat(prr.getState()).isEqualTo(ResultStatus.DID_NOT_FINISH);
    }

    @Test
    void ranking_twoNonEligibleWithSameRuntime_triggersTiebreaker() {
        // Two non-eligible runners with the SAME best runtime → thenComparingLong(personId) is invoked
        long p1 = 20L, p2 = 21L;
        PersonRaceResult r1 = makeRaceResult("M21", p1, 800.0, ResultStatus.OK, (byte) 1);
        PersonRaceResult r2 = makeRaceResult("M21", p2, 800.0, ResultStatus.OK, (byte) 1);
        PersonResult ne1 = PersonResult.of(ClassResultShortName.of("M21"), PersonId.of(p1), nonEligibleClubId, List.of(r1));
        PersonResult ne2 = PersonResult.of(ClassResultShortName.of("M21"), PersonId.of(p2), nonEligibleClubId, List.of(r2));
        ClassResult cr = ClassResult.of("M21", "M21", Gender.M, List.of(ne1, ne2), null);
        ResultList source = new ResultList(
                ResultListId.of(1L), eventId, RaceId.of(1L), "test", null, null, List.of(cr));

        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(source));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(orgTree());
        Race race0 = Race.of(RaceId.of(100L), eventId, null, (byte) 0);
        when(raceRepository.findOrCreate(any(Race.class))).thenReturn(race0);
        when(resultListRepository.save(any(ResultList.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ResultList> result = service.addChampionshipRanking(eventId, baseOrgId, Set.of());

        assertThat(result).hasSize(1);
        List<PersonRaceResult> prrs = result.getFirst().getClassResults().stream()
                .flatMap(c -> c.personResults().value().stream())
                .flatMap(p -> p.personRaceResults().value().stream())
                .toList();
        assertThat(prrs).hasSize(2);
        assertThat(prrs.stream().map(r -> r.getState()).toList())
                .containsOnly(ResultStatus.NOT_COMPETING);
    }

    @Test
    void ranking_twoNonEligibleWithoutTime_triggersSortByPersonId() {
        // Two non-eligible runners with null runtime → sorted only by personId
        long p1 = 30L, p2 = 31L;
        PersonRaceResult r1 = new PersonRaceResult(
                ClassResultShortName.of("M21"), PersonId.of(p1),
                DateTime.empty(), DateTime.empty(), PunchTime.of((Double) null),
                Position.of(1L), ResultStatus.DID_NOT_FINISH, RaceNumber.of((byte) 1), null);
        PersonRaceResult r2 = new PersonRaceResult(
                ClassResultShortName.of("M21"), PersonId.of(p2),
                DateTime.empty(), DateTime.empty(), PunchTime.of((Double) null),
                Position.of(2L), ResultStatus.DID_NOT_FINISH, RaceNumber.of((byte) 1), null);
        PersonResult ne1 = PersonResult.of(ClassResultShortName.of("M21"), PersonId.of(p1), nonEligibleClubId, List.of(r1));
        PersonResult ne2 = PersonResult.of(ClassResultShortName.of("M21"), PersonId.of(p2), nonEligibleClubId, List.of(r2));
        ClassResult cr = ClassResult.of("M21", "M21", Gender.M, List.of(ne2, ne1), null);
        ResultList source = new ResultList(
                ResultListId.of(1L), eventId, RaceId.of(1L), "test", null, null, List.of(cr));

        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(source));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(orgTree());
        Race race0 = Race.of(RaceId.of(100L), eventId, null, (byte) 0);
        when(raceRepository.findOrCreate(any(Race.class))).thenReturn(race0);
        when(resultListRepository.save(any(ResultList.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ResultList> result = service.addChampionshipRanking(eventId, baseOrgId, Set.of());

        assertThat(result).hasSize(1);
        List<Long> personIds = result.getFirst().getClassResults().stream()
                .flatMap(c -> c.personResults().value().stream())
                .map(p -> p.personId().value())
                .toList();
        // Sorted ascending by personId
        assertThat(personIds).containsExactly(30L, 31L);
    }

    @Test
    void cleanup_twoNonEligibleWithSameRuntime_triggersSortTiebreaker() {
        // Two non-eligible persons with same runtime → reorderPositions thenComparingLong is invoked
        long p1 = 40L, p2 = 41L;
        PersonRaceResult r1 = makeRaceResult("M21", p1, 750.0, ResultStatus.OK, (byte) 1);
        PersonRaceResult r2 = makeRaceResult("M21", p2, 750.0, ResultStatus.OK, (byte) 1);
        PersonResult ne1 = PersonResult.of(ClassResultShortName.of("M21"), PersonId.of(p1), nonEligibleClubId, List.of(r1));
        PersonResult ne2 = PersonResult.of(ClassResultShortName.of("M21"), PersonId.of(p2), nonEligibleClubId, List.of(r2));
        ClassResult cr = ClassResult.of("M21", "M21", Gender.M, List.of(ne2, ne1), null);
        ResultList resultList = new ResultList(
                ResultListId.of(1L), eventId, RaceId.of(1L), "test", null, null, List.of(cr));

        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(resultList));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(orgTree());

        service.applyChampionshipCleanup(eventId, baseOrgId, Set.of());

        verify(resultListRepository).update(resultList);
        List<Long> sortedPersonIds = resultList.getClassResults().stream()
                .flatMap(c -> c.personResults().value().stream())
                .map(p -> p.personId().value())
                .toList();
        // Both NOT_COMPETING with same runtime → sorted by personId ascending
        assertThat(sortedPersonIds).containsExactly(40L, 41L);
    }

    @Test
    void ranking_eligibleWithOkStatusAndNoRuntime_fallsBackToMissingPunch() {
        // Eligible person with OK status but null runtime:
        // bestRuntime()=MAX_VALUE → hasOkResult=false → bestSourceStatus() called
        // All states are OK → filter excludes them → orElse(MISSING_PUNCH) returned
        PersonRaceResult raceResult = new PersonRaceResult(
                ClassResultShortName.of("H21"), PersonId.of(ELIGIBLE_PERSON_ID),
                DateTime.empty(), DateTime.empty(),
                PunchTime.of((Double) null),
                Position.of(1L), ResultStatus.OK,
                RaceNumber.of((byte) 1), null);
        PersonResult eligiblePerson = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(ELIGIBLE_PERSON_ID),
                eligibleClubId, List.of(raceResult));
        ClassResult classResult = ClassResult.of("H21", "H21", Gender.M, List.of(eligiblePerson), null);
        ResultList sourceResultList = new ResultList(
                ResultListId.of(1L), eventId, RaceId.of(1L), "test", null, null, List.of(classResult));

        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(sourceResultList));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(orgTree());
        Race race0 = Race.of(RaceId.of(100L), eventId, null, (byte) 0);
        when(raceRepository.findOrCreate(any(Race.class))).thenReturn(race0);
        when(resultListRepository.save(any(ResultList.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ResultList> result = service.addChampionshipRanking(eventId, baseOrgId, Set.of());

        assertThat(result).hasSize(1);
        PersonRaceResult prr = result.getFirst().getClassResults().stream().findFirst().orElseThrow()
                .personResults().value().stream().findFirst().orElseThrow()
                .personRaceResults().value().stream().findFirst().orElseThrow();
        assertThat(prr.getState()).isEqualTo(ResultStatus.MISSING_PUNCH);
    }

    @Test
    void ranking_eligibleWithNoValidTime_preservesNonOkStatus() {
        // Eligible person (wrong club → ineligible) with null runtime: goes to nonEligibleWithoutTime
        // Eligible person FROM eligible club but with no time: goes to eligible-no-time path
        PersonRaceResult raceResult = new PersonRaceResult(
                ClassResultShortName.of("H21"), PersonId.of(ELIGIBLE_PERSON_ID),
                DateTime.empty(), DateTime.empty(),
                PunchTime.of((Double) null),
                Position.of(1L), ResultStatus.MISSING_PUNCH,
                RaceNumber.of((byte) 1), null);
        PersonResult eligiblePerson = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(ELIGIBLE_PERSON_ID),
                eligibleClubId, List.of(raceResult));
        ClassResult classResult = ClassResult.of("H21", "H21", Gender.M, List.of(eligiblePerson), null);
        ResultList sourceResultList = new ResultList(
                ResultListId.of(1L), eventId, RaceId.of(1L), "test", null, null, List.of(classResult));

        when(resultListRepository.findByEventId(eventId)).thenReturn(List.of(sourceResultList));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(orgTree());
        Race race0 = Race.of(RaceId.of(100L), eventId, null, (byte) 0);
        when(raceRepository.findOrCreate(any(Race.class))).thenReturn(race0);
        when(resultListRepository.save(any(ResultList.class))).thenAnswer(inv -> inv.getArgument(0));

        List<ResultList> result = service.addChampionshipRanking(eventId, baseOrgId, Set.of());

        assertThat(result).hasSize(1);
        PersonRaceResult prr = result.getFirst().getClassResults().stream().findFirst().orElseThrow()
                .personResults().value().stream().findFirst().orElseThrow()
                .personRaceResults().value().stream().findFirst().orElseThrow();
        assertThat(prr.getState()).isEqualTo(ResultStatus.MISSING_PUNCH);
    }
}
