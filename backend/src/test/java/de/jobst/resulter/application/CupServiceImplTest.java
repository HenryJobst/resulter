package de.jobst.resulter.application;

import de.jobst.resulter.application.port.*;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.domain.aggregations.CupDetailed;
import de.jobst.resulter.domain.util.ResourceNotFoundException;
import de.jobst.resulter.springapp.config.SpringSecurityAuditorAware;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CupServiceImplTest {

    @Mock CupRepository cupRepository;
    @Mock OrganisationRepository organisationRepository;
    @Mock OrganisationService organisationService;
    @Mock RaceService raceService;
    @Mock ResultListService resultListService;
    @Mock EventService eventService;
    @Mock CupScoreListRepository cupScoreListRepository;
    @Mock SpringSecurityAuditorAware springSecurityAuditorAware;
    @Mock PersonRepository personRepository;

    @InjectMocks
    CupServiceImpl service;

    private Cup cup(long id) {
        return Cup.of(id, "MyCup", CupType.ADD, Year.of(2024), List.of());
    }

    @Test
    void findAll_delegatesToRepository() {
        when(cupRepository.findAll()).thenReturn(List.of(cup(1L)));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void findOrCreate_delegatesToRepository() {
        Cup c = cup(1L);
        when(cupRepository.findOrCreate(c)).thenReturn(c);
        assertThat(service.findOrCreate(c)).isEqualTo(c);
    }

    @Test
    void findById_returnsPresent_whenFound() {
        Cup c = cup(1L);
        when(cupRepository.findById(CupId.of(1L))).thenReturn(Optional.of(c));
        assertThat(service.findById(CupId.of(1L))).isPresent();
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        when(cupRepository.findById(CupId.of(99L))).thenReturn(Optional.empty());
        assertThat(service.findById(CupId.of(99L))).isEmpty();
    }

    @Test
    void getById_returnsCup_whenFound() {
        Cup c = cup(1L);
        when(cupRepository.findById(CupId.of(1L))).thenReturn(Optional.of(c));
        assertThat(service.getById(CupId.of(1L))).isEqualTo(c);
    }

    @Test
    void getById_throws_whenNotFound() {
        when(cupRepository.findById(CupId.of(99L))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(CupId.of(99L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createCup_savesNewCup() {
        Cup c = cup(1L);
        when(cupRepository.save(any(Cup.class))).thenReturn(c);
        Cup result = service.createCup("MyCup", CupType.ADD, Year.of(2024), List.of());
        verify(cupRepository).save(any(Cup.class));
        assertThat(result).isEqualTo(c);
    }

    @Test
    void deleteCup_callsDeleteOnRepository() {
        Cup c = cup(1L);
        when(cupRepository.findById(CupId.of(1L))).thenReturn(Optional.of(c));
        service.deleteCup(CupId.of(1L));
        verify(cupRepository).deleteCup(c);
    }

    @Test
    void deleteCup_throws_whenCupNotFound() {
        when(cupRepository.findById(CupId.of(99L))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteCup(CupId.of(99L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCup_savesUpdatedCup() {
        Cup c = cup(1L);
        when(cupRepository.findById(CupId.of(1L))).thenReturn(Optional.of(c));
        when(eventService.findAllById(any())).thenReturn(List.of());
        when(cupRepository.save(any(Cup.class))).thenReturn(c);
        Cup result = service.updateCup(CupId.of(1L), CupName.of("Neu"), CupType.NOR, Year.of(2025), List.of());
        verify(cupRepository).save(any(Cup.class));
        assertThat(result).isEqualTo(c);
    }

    @Test
    void findAll_paged_delegatesToRepository() {
        when(cupRepository.findAll(any(), any()))
                .thenReturn(new PageImpl<>(List.of(cup(1L))));
        var page = service.findAll(null, PageRequest.of(0, 10));
        assertThat(page).isNotEmpty();
    }

    @Test
    void getCupDetailed_shouldReturnResult_whenCupHasNoEvents() {
        Cup c = cup(1L); // no eventIds
        when(cupRepository.findById(CupId.of(1L))).thenReturn(Optional.of(c));

        CupDetailed result = service.getCupDetailed(CupId.of(1L));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(c.getId());
    }

    @Test
    void calculateScore_shouldReturnEmpty_whenCupHasNoEvents() {
        Cup c = cup(1L); // no eventIds
        when(cupRepository.findById(CupId.of(1L))).thenReturn(Optional.of(c));
        when(springSecurityAuditorAware.getCurrentAuditor()).thenReturn(Optional.of("test-user"));
        when(cupScoreListRepository.saveAll(any())).thenReturn(List.of());

        List<CupScoreList> result = service.calculateScore(CupId.of(1L));

        assertThat(result).isEmpty();
    }

    @Test
    void calculateScore_shouldInvokeSaveAll_whenSingleResultListExists() {
        Cup c = Cup.of(1L, "MyCup", CupType.ADD, Year.of(2024), List.of(EventId.of(1L)));
        Event event = Event.of(1L, "TestEvent");
        // EventId.of(99L) is not in cup.eventIds → ResultList.calculate() returns null (invalid)
        // classResults=null → getRaceNumber() returns RaceNumber.empty() (no NPE in log)
        ResultList rl = new ResultList(ResultListId.of(1L), EventId.of(99L), RaceId.empty(),
                null, null, null, null);

        when(cupRepository.findById(CupId.of(1L))).thenReturn(Optional.of(c));
        when(eventService.getByIds(any())).thenReturn(List.of(event));
        when(resultListService.findAllByEventIds(any())).thenReturn(Map.of(EventId.of(1L), List.of(rl)));

        service.calculateScore(CupId.of(1L));

        verify(cupScoreListRepository).saveAll(any());
        verify(cupScoreListRepository).deleteAllByDomainKey(any());
    }

    @Test
    void getCupDetailed_shouldReturnResult_whenCupHasEventAndResultList() {
        Cup c = Cup.of(1L, "MyCup", CupType.ADD, Year.of(2024), List.of(EventId.of(1L)));
        Event event = Event.of(1L, "TestEvent");
        Race race = Race.of(RaceId.of(1L), EventId.of(1L), null, (byte) 1);
        // classResults=null → getRaceNumber() returns RaceNumber.empty() (safe for log calls)
        ResultList rl = new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.of(1L),
                null, null, null, null);

        when(cupRepository.findById(CupId.of(1L))).thenReturn(Optional.of(c));
        when(raceService.findAllByEventIds(any())).thenReturn(List.of(race));
        when(eventService.findAllByIdAsMap(any())).thenReturn(Map.of(EventId.of(1L), event));
        when(resultListService.findAllByEventIds(any())).thenReturn(Map.of(EventId.of(1L), List.of(rl)));

        CupDetailed result = service.getCupDetailed(CupId.of(1L));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(c.getId());
    }

    @Test
    void getCupDetailed_shouldCalculateStatistics_withClassResultsForNebelCup() {
        PersonRaceResult prr = PersonRaceResult.of("H21", 1L, null, null, 100.0, 1L, (byte) 1, ResultStatus.OK);
        PersonResult pr = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(1L), OrganisationId.of(1L), List.of(prr));
        ClassResult cr = ClassResult.of("H21", "H21", Gender.M, List.of(pr), null);
        Cup c = Cup.of(1L, "MyCup", CupType.NEBEL, Year.of(2024), List.of(EventId.of(1L)));
        Event event = Event.of(1L, "TestEvent");
        Race race = Race.of(RaceId.of(1L), EventId.of(1L), null, (byte) 1);
        ResultList rl = new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.of(1L),
                null, null, null, List.of(cr));
        Organisation org = Organisation.of(1L, "TestOrg", "TO");

        when(cupRepository.findById(CupId.of(1L))).thenReturn(Optional.of(c));
        when(raceService.findAllByEventIds(any())).thenReturn(List.of(race));
        when(eventService.findAllByIdAsMap(any())).thenReturn(Map.of(EventId.of(1L), event));
        when(resultListService.findAllByEventIds(any())).thenReturn(Map.of(EventId.of(1L), List.of(rl)));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(Map.of(OrganisationId.of(1L), org));

        CupDetailed result = service.getCupDetailed(CupId.of(1L));

        assertThat(result).isNotNull();
        assertThat(result.getCupStatistics().overallStatistics().totalStarts()).isEqualTo(1);
        assertThat(result.getCupStatistics().overallStatistics().totalRunners()).isEqualTo(1);
        assertThat(result.getCupStatistics().organisationStatistics()).hasSize(1);
    }

    @Test
    void getCupDetailed_shouldAggregatePersonScores_withCupScoreLists() {
        Cup c = Cup.of(1L, "MyCup", CupType.ADD, Year.of(2024), List.of(EventId.of(1L)));
        Event event = Event.of(1L, "TestEvent");
        Race race = Race.of(RaceId.of(1L), EventId.of(1L), null, (byte) 1);
        ResultList rl = new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.of(1L),
                null, null, null, null);
        CupScore score = CupScore.of(PersonId.of(1L), null, ClassResultShortName.of("H21"), 100.0);
        CupScoreList csl = new CupScoreList(CupScoreListId.empty(), CupId.of(1L), ResultListId.of(1L),
                List.of(score), null, null);

        when(cupRepository.findById(CupId.of(1L))).thenReturn(Optional.of(c));
        when(raceService.findAllByEventIds(any())).thenReturn(List.of(race));
        when(eventService.findAllByIdAsMap(any())).thenReturn(Map.of(EventId.of(1L), event));
        when(resultListService.findAllByEventIds(any())).thenReturn(Map.of(EventId.of(1L), List.of(rl)));
        when(resultListService.getCupScoreListsByResultListIds(any(), any()))
                .thenReturn(Map.of(ResultListId.of(1L), List.of(csl)));

        CupDetailed result = service.getCupDetailed(CupId.of(1L));

        assertThat(result).isNotNull();
        assertThat(result.getAggregatedPersonScoresList()).hasSize(1);
    }

    @Test
    void getCupDetailed_shouldReturnResult_whenCupTypeIsOrganisationGrouped() {
        Cup c = Cup.of(1L, "MyCup", CupType.KJ, Year.of(2024), List.of(EventId.of(2L)));
        Event event = Event.of(2L, "TestEvent");
        Race race = Race.of(RaceId.of(2L), EventId.of(2L), null, (byte) 1);
        // classResults=null → getRaceNumber() returns RaceNumber.empty() (safe for log calls)
        ResultList rl = new ResultList(ResultListId.of(2L), EventId.of(2L), RaceId.of(2L),
                null, null, null, null);

        when(cupRepository.findById(CupId.of(1L))).thenReturn(Optional.of(c));
        when(raceService.findAllByEventIds(any())).thenReturn(List.of(race));
        when(eventService.findAllByIdAsMap(any())).thenReturn(Map.of(EventId.of(2L), event));
        when(resultListService.findAllByEventIds(any())).thenReturn(Map.of(EventId.of(2L), List.of(rl)));

        CupDetailed result = service.getCupDetailed(CupId.of(1L));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(c.getId());
    }

    @Test
    void getCupDetailed_shouldCountNonScoringStarts_whenPersonHasMissingPunch() {
        // Covers: totalNonScoringStarts increment in calculateCupStatistics
        PersonRaceResult prrMissing = PersonRaceResult.of(
                "H21", 2L, null, null, null, 1L, (byte) 1, ResultStatus.MISSING_PUNCH);
        PersonResult pr = PersonResult.of(
                ClassResultShortName.of("H21"), PersonId.of(2L), OrganisationId.of(2L), List.of(prrMissing));
        ClassResult cr = ClassResult.of("H21", "H21", Gender.M, List.of(pr), null);
        Cup c = Cup.of(1L, "MyCup", CupType.NEBEL, Year.of(2024), List.of(EventId.of(4L)));
        Event event = Event.of(4L, "TestEvent");
        Race race = Race.of(RaceId.of(4L), EventId.of(4L), null, (byte) 1);
        ResultList rl = new ResultList(ResultListId.of(4L), EventId.of(4L), RaceId.of(4L),
                null, null, null, List.of(cr));
        Organisation org = Organisation.of(2L, "TestOrg2", "TO");

        when(cupRepository.findById(CupId.of(1L))).thenReturn(Optional.of(c));
        when(raceService.findAllByEventIds(any())).thenReturn(List.of(race));
        when(eventService.findAllByIdAsMap(any())).thenReturn(Map.of(EventId.of(4L), event));
        when(resultListService.findAllByEventIds(any())).thenReturn(Map.of(EventId.of(4L), List.of(rl)));
        when(organisationRepository.loadOrganisationTree(any()))
                .thenReturn(Map.of(OrganisationId.of(2L), org));
        when(personRepository.findAllById(any())).thenReturn(Map.of());

        CupDetailed result = service.getCupDetailed(CupId.of(1L));

        assertThat(result.getCupStatistics().overallStatistics().totalStarts()).isEqualTo(1);
        assertThat(result.getCupStatistics().overallStatistics().totalNonScoringStarts()).isEqualTo(1);
    }

    @Test
    void getCupDetailed_shouldBuildOrganisationScores_whenKjCupHasScores() {
        // Covers calculateOrganisationGroupedSums if-branch (mainCupScoreList.isPresent())
        Cup c = Cup.of(1L, "MyCup", CupType.KJ, Year.of(2024), List.of(EventId.of(3L)));
        Event event = Event.of(3L, "TestEvent");
        Race race = Race.of(RaceId.of(3L), EventId.of(3L), null, (byte) 1);
        ResultList rl = new ResultList(ResultListId.of(3L), EventId.of(3L), RaceId.of(3L),
                null, null, null, null);
        CupScore score = CupScore.of(PersonId.of(1L), OrganisationId.of(1L),
                ClassResultShortName.of("H14"), 100.0);
        CupScoreList csl = new CupScoreList(CupScoreListId.empty(), CupId.of(1L),
                ResultListId.of(3L), List.of(score), null, null);
        Organisation org = Organisation.of(1L, "TestOrg", "TO");

        when(cupRepository.findById(CupId.of(1L))).thenReturn(Optional.of(c));
        when(raceService.findAllByEventIds(any())).thenReturn(List.of(race));
        when(eventService.findAllByIdAsMap(any())).thenReturn(Map.of(EventId.of(3L), event));
        when(resultListService.findAllByEventIds(any()))
                .thenReturn(Map.of(EventId.of(3L), List.of(rl)));
        when(resultListService.getCupScoreListsByResultListIds(any(), any()))
                .thenReturn(Map.of(ResultListId.of(3L), List.of(csl)));
        // Empty organisationById → KJStrategy.validClubs empty → all orgs valid
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(Map.of());
        when(organisationService.findAllById(any())).thenReturn(List.of(org));
        when(personRepository.findAllById(any())).thenReturn(Map.of());

        CupDetailed result = service.getCupDetailed(CupId.of(1L));

        assertThat(result).isNotNull();
        assertThat(result.getAggregatedPersonScoresList()).isEmpty(); // KJ is org-grouped → empty
    }

}
