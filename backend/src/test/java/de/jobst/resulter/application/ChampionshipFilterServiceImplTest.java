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
        when(resultListRepository.findOrCreate(any(ResultList.class))).thenReturn(savedResultList);

        List<ResultList> result = service.addChampionshipRanking(eventId, baseOrgId, Set.of());

        assertThat(result).hasSize(1);
        verify(resultListRepository).findOrCreate(any(ResultList.class));

        // Inspect the argument passed to findOrCreate
        org.mockito.ArgumentCaptor<ResultList> captor = org.mockito.ArgumentCaptor.forClass(ResultList.class);
        verify(resultListRepository).findOrCreate(captor.capture());
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
        when(resultListRepository.findOrCreate(any(ResultList.class))).thenReturn(savedResultList);

        service.addChampionshipRanking(eventId, baseOrgId, Set.of());

        org.mockito.ArgumentCaptor<ResultList> captor = org.mockito.ArgumentCaptor.forClass(ResultList.class);
        verify(resultListRepository).findOrCreate(captor.capture());
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
        when(resultListRepository.findOrCreate(any(ResultList.class))).thenAnswer(inv -> inv.getArgument(0));

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
}
