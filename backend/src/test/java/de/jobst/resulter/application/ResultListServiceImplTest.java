package de.jobst.resulter.application;

import de.jobst.resulter.application.port.*;
import de.jobst.resulter.domain.*;
import de.jobst.resulter.springapp.config.SpringSecurityAuditorAware;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultListServiceImplTest {

    @Mock ResultListRepository resultListRepository;
    @Mock CupRepository cupRepository;
    @Mock EventRepository eventRepository;
    @Mock OrganisationRepository organisationRepository;
    @Mock PersonRepository personRepository;
    @Mock CertificateService certificateService;
    @Mock EventCertificateStatRepository eventCertificateStatRepository;
    @Mock CupScoreListRepository cupScoreListRepository;
    @Mock SpringSecurityAuditorAware springSecurityAuditorAware;
    @Mock EventCertificateService eventCertificateService;
    @Mock MediaFileService mediaFileService;
    @Mock SplitTimeListRepository splitTimeListRepository;

    @InjectMocks
    ResultListServiceImpl service;

    private static ResultList resultList(long id) {
        return new ResultList(ResultListId.of(id), EventId.of(1L), RaceId.empty(), null, null, null, null);
    }

    @Test
    void findOrCreate_delegatesToRepository() {
        ResultList rl = resultList(1L);
        when(resultListRepository.findOrCreate(rl)).thenReturn(rl);
        assertThat(service.findOrCreate(rl)).isEqualTo(rl);
    }

    @Test
    void findById_returnsPresent_whenFound() {
        when(resultListRepository.findById(ResultListId.of(1L))).thenReturn(Optional.of(resultList(1L)));
        assertThat(service.findById(ResultListId.of(1L))).isPresent();
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        when(resultListRepository.findById(ResultListId.of(99L))).thenReturn(Optional.empty());
        assertThat(service.findById(ResultListId.of(99L))).isEmpty();
    }

    @Test
    void findAll_delegatesToRepository() {
        when(resultListRepository.findAll()).thenReturn(List.of(resultList(1L)));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void update_delegatesToRepository() {
        ResultList rl = resultList(1L);
        when(resultListRepository.update(rl)).thenReturn(rl);
        assertThat(service.update(rl)).isEqualTo(rl);
    }

    @Test
    void findByEventId_delegatesToRepository() {
        when(resultListRepository.findByEventId(EventId.of(1L))).thenReturn(List.of(resultList(1L)));
        Collection<ResultList> result = service.findByEventId(EventId.of(1L));
        assertThat(result).hasSize(1);
    }

    @Test
    void findAllByEventIds_returnsEmpty_forNullInput() {
        assertThat(service.findAllByEventIds(null)).isEmpty();
    }

    @Test
    void findAllByEventIds_returnsEmpty_forEmptyInput() {
        assertThat(service.findAllByEventIds(List.of())).isEmpty();
    }

    @Test
    void findAllByEventIds_groupsByEventId() {
        when(resultListRepository.findAllByEventIds(any()))
                .thenReturn(List.of(resultList(1L), resultList(2L)));
        Map<EventId, List<ResultList>> result = service.findAllByEventIds(List.of(EventId.of(1L)));
        assertThat(result).containsKey(EventId.of(1L));
    }

    @Test
    void findResultListIdsWithSplitTimes_returnsEmpty_forNull() {
        assertThat(service.findResultListIdsWithSplitTimes(null)).isEmpty();
    }

    @Test
    void findResultListIdsWithSplitTimes_returnsEmpty_forEmpty() {
        assertThat(service.findResultListIdsWithSplitTimes(List.of())).isEmpty();
    }

    @Test
    void findResultListIdsWithSplitTimes_delegatesToRepository() {
        when(splitTimeListRepository.existsByResultListIds(any()))
                .thenReturn(Set.of(ResultListId.of(1L)));
        var result = service.findResultListIdsWithSplitTimes(List.of(ResultListId.of(1L)));
        assertThat(result).containsExactly(ResultListId.of(1L));
    }

    @Test
    void calculateScore_returnsEmpty_whenResultListNotFound() {
        when(resultListRepository.findById(ResultListId.of(99L))).thenReturn(Optional.empty());
        assertThat(service.calculateScore(ResultListId.of(99L))).isEmpty();
    }

    @Test
    void calculateScore_returnsEmpty_whenClassResultsNull() {
        when(resultListRepository.findById(ResultListId.of(1L))).thenReturn(Optional.of(resultList(1L)));
        assertThat(service.calculateScore(ResultListId.of(1L))).isEmpty();
    }

    @Test
    void getCertificateStats_delegatesToRepository() {
        EventCertificateStat stat = EventCertificateStat.of(null, EventId.of(1L), PersonId.of(1L), Instant.now());
        when(eventCertificateStatRepository.findAllByEvent(EventId.of(1L))).thenReturn(List.of(stat));
        assertThat(service.getCertificateStats(EventId.of(1L))).hasSize(1);
    }

    @Test
    void deleteEventCertificateStat_delegatesToRepository() {
        EventCertificateStatId id = EventCertificateStatId.of(1L);
        service.deleteEventCertificateStat(id);
        verify(eventCertificateStatRepository).deleteById(id);
    }

    @Test
    void getCupScoreLists_byResultListId_delegatesToRepository() {
        when(cupScoreListRepository.findAllByResultListId(ResultListId.of(1L))).thenReturn(List.of());
        assertThat(service.getCupScoreLists(ResultListId.of(1L))).isEmpty();
    }

    @Test
    void getCupScoreLists_byResultListIdAndCupId_delegatesToRepository() {
        when(cupScoreListRepository.findAllByResultListIdAndCupId(ResultListId.of(1L), CupId.of(1L)))
                .thenReturn(List.of());
        assertThat(service.getCupScoreLists(ResultListId.of(1L), CupId.of(1L))).isEmpty();
    }

    @Test
    void getCupScoreListsByResultListIds_delegatesToRepository() {
        when(cupScoreListRepository.findAllByResultListIdsAndCupId(any(), any()))
                .thenReturn(Map.of());
        assertThat(service.getCupScoreListsByResultListIds(List.of(ResultListId.of(1L)), CupId.of(1L))).isEmpty();
    }

    @Test
    void calculateScore_returnsEmpty_whenCupsEmpty() {
        ClassResult cr = ClassResult.of("H21", "H21", Gender.M, List.of(), null);
        ResultList rl = new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.empty(),
                null, null, null, List.of(cr));
        when(resultListRepository.findById(ResultListId.of(1L))).thenReturn(Optional.of(rl));
        when(cupRepository.findByEvent(EventId.of(1L))).thenReturn(List.of());
        assertThat(service.calculateScore(ResultListId.of(1L))).isEmpty();
    }

    @Test
    void createCertificate_returnsNull_whenResultListNull() {
        when(resultListRepository.findByResultListIdAndClassResultShortNameAndPersonId(any(), any(), any()))
                .thenReturn(null);
        assertThat(service.createCertificate(
                ResultListId.of(1L), ClassResultShortName.of("H21"), PersonId.of(1L)))
                .isNull();
    }

    @Test
    void createCertificate_returnsNull_whenClassResultsNull() {
        when(resultListRepository.findByResultListIdAndClassResultShortNameAndPersonId(any(), any(), any()))
                .thenReturn(resultList(1L));
        assertThat(service.createCertificate(
                ResultListId.of(1L), ClassResultShortName.of("H21"), PersonId.of(1L)))
                .isNull();
    }

    @Test
    void createCertificate_returnsNull_whenPersonNotFound() {
        ClassResult cr = ClassResult.of("H21", "H21", Gender.M, List.of(), null);
        ResultList rl = new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.empty(),
                null, null, null, List.of(cr));
        when(resultListRepository.findByResultListIdAndClassResultShortNameAndPersonId(any(), any(), any()))
                .thenReturn(rl);
        when(personRepository.findById(any())).thenReturn(Optional.empty());
        assertThat(service.createCertificate(
                ResultListId.of(1L), ClassResultShortName.of("H21"), PersonId.of(1L)))
                .isNull();
    }

    @Test
    void createCertificate_returnsNull_whenEventNotFound() {
        ClassResult cr = ClassResult.of("H21", "H21", Gender.M, List.of(), null);
        ResultList rl = new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.empty(),
                null, null, null, List.of(cr));
        Person person = Person.of(1L, "Doe", "Jane", null, Gender.F);
        when(resultListRepository.findByResultListIdAndClassResultShortNameAndPersonId(any(), any(), any()))
                .thenReturn(rl);
        when(personRepository.findById(any())).thenReturn(Optional.of(person));
        when(eventRepository.findById(EventId.of(1L))).thenReturn(Optional.empty());
        assertThat(service.createCertificate(
                ResultListId.of(1L), ClassResultShortName.of("H21"), PersonId.of(1L)))
                .isNull();
    }

    @Test
    void createCertificate_returnsNull_whenPersonResultNotFound() {
        ClassResult cr = ClassResult.of("H21", "H21", Gender.M, List.of(), null);
        ResultList rl = new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.empty(),
                null, null, null, List.of(cr));
        Person person = Person.of(1L, "Doe", "Jane", null, Gender.F);
        Event event = Event.of("TestEvent");
        when(resultListRepository.findByResultListIdAndClassResultShortNameAndPersonId(any(), any(), any()))
                .thenReturn(rl);
        when(personRepository.findById(any())).thenReturn(Optional.of(person));
        when(eventRepository.findById(any())).thenReturn(Optional.of(event));
        assertThat(service.createCertificate(
                ResultListId.of(1L), ClassResultShortName.of("H21"), PersonId.of(1L)))
                .isNull();
    }

    @Test
    void createCertificate_byEventAndCertificate_delegatesToCertificateService() {
        Event event = Event.of("TestEvent");
        EventCertificate cert = mock(EventCertificate.class);
        when(certificateService.createCertificate(any(), any(), any())).thenReturn(null);
        service.createCertificate(event, cert);
        verify(certificateService).createCertificate(event, cert, mediaFileService);
    }

    @Test
    void calculateScore_returnsEmpty_whenClassResultsEmpty() {
        ResultList rl = new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.empty(),
                null, null, null, List.of());
        when(resultListRepository.findById(ResultListId.of(1L))).thenReturn(Optional.of(rl));
        assertThat(service.calculateScore(ResultListId.of(1L))).isEmpty();
    }

    @Test
    void calculateScore_deletesEventWide_whenSingleResultList() {
        ClassResult cr = ClassResult.of("H21", "H21", Gender.M, List.of(), null);
        ResultList rl = new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.empty(),
                null, null, null, List.of(cr));
        Cup cup = Cup.of(1L, "TestCup", CupType.ADD, Year.of(2024), List.of(EventId.of(1L)));

        when(resultListRepository.findById(ResultListId.of(1L))).thenReturn(Optional.of(rl));
        when(cupRepository.findByEvent(EventId.of(1L))).thenReturn(List.of(cup));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(Map.of());
        when(springSecurityAuditorAware.getCurrentAuditor()).thenReturn(Optional.of("user"));
        // 1 ResultList → shouldDeleteEventWide = true → deleteAllByEventId
        when(resultListRepository.findByEventId(EventId.of(1L))).thenReturn(List.of(rl));
        when(cupScoreListRepository.saveAll(any())).thenReturn(List.of());

        service.calculateScore(ResultListId.of(1L));

        verify(cupScoreListRepository).deleteAllByEventId(EventId.of(1L));
        verify(cupScoreListRepository, never()).deleteAllByDomainKey(any());
    }

    @Test
    void calculateScore_deletesByDomainKey_whenMultipleResultListsOnDifferentDays() {
        ClassResult cr = ClassResult.of("H21", "H21", Gender.M, List.of(), null);
        ZonedDateTime day1 = ZonedDateTime.of(2024, 10, 1, 10, 0, 0, 0, java.time.ZoneOffset.UTC);
        ZonedDateTime day2 = ZonedDateTime.of(2024, 10, 2, 10, 0, 0, 0, java.time.ZoneOffset.UTC);
        ResultList rl1 = new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.empty(),
                null, day1, null, List.of(cr));
        ResultList rl2 = new ResultList(ResultListId.of(2L), EventId.of(1L), RaceId.empty(),
                null, day2, null, null);
        Cup cup = Cup.of(1L, "TestCup", CupType.ADD, Year.of(2024), List.of(EventId.of(1L)));

        when(resultListRepository.findById(ResultListId.of(1L))).thenReturn(Optional.of(rl1));
        when(cupRepository.findByEvent(EventId.of(1L))).thenReturn(List.of(cup));
        when(organisationRepository.loadOrganisationTree(any())).thenReturn(Map.of());
        when(springSecurityAuditorAware.getCurrentAuditor()).thenReturn(Optional.of("user"));
        // 2 ResultLists auf verschiedenen Tagen → shouldDeleteEventWide = false → deleteAllByDomainKey
        when(resultListRepository.findByEventId(EventId.of(1L))).thenReturn(List.of(rl1, rl2));
        when(cupScoreListRepository.saveAll(any())).thenReturn(List.of());

        service.calculateScore(ResultListId.of(1L));

        verify(cupScoreListRepository).deleteAllByDomainKey(any());
        verify(cupScoreListRepository, never()).deleteAllByEventId(any());
    }

    @Test
    void createCertificate_returnsNull_whenPersonRaceResultNotFound() {
        // PersonResult gefunden, aber keine PersonRaceResults
        PersonResult pr = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(1L), null, List.of());
        ClassResult cr = ClassResult.of("H21", "H21", Gender.M, List.of(pr), null);
        ResultList rl = new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.empty(),
                null, null, null, List.of(cr));
        Person person = Person.of(1L, "Doe", "Jane", null, Gender.F);
        Event event = Event.of("TestEvent");

        when(resultListRepository.findByResultListIdAndClassResultShortNameAndPersonId(any(), any(), any()))
                .thenReturn(rl);
        when(personRepository.findById(any())).thenReturn(Optional.of(person));
        when(eventRepository.findById(any())).thenReturn(Optional.of(event));

        assertThat(service.createCertificate(
                ResultListId.of(1L), ClassResultShortName.of("H21"), PersonId.of(1L)))
                .isNull();
    }

    @Test
    void createCertificate_returnsCertificate_whenAllDataPresent() {
        PersonRaceResult prr = PersonRaceResult.of("H21", 1L, null, null, 100.0, 1L, (byte) 1, ResultStatus.OK);
        PersonResult pr = PersonResult.of(ClassResultShortName.of("H21"), PersonId.of(1L),
                OrganisationId.of(1L), List.of(prr));
        ClassResult cr = ClassResult.of("H21", "H21", Gender.M, List.of(pr), null);
        ResultList rl = new ResultList(ResultListId.of(1L), EventId.of(1L), RaceId.empty(),
                null, null, null, List.of(cr));
        Person person = Person.of(1L, "Doe", "Jane", null, Gender.F);
        Event event = Event.of(1L, "TestEvent", null, null, List.of(), null,
                EventCertificateId.of(1L), Discipline.getDefault(), false);
        Organisation org = Organisation.of(1L, "TestOrg", "TO");
        EventCertificate eventCert = EventCertificate.of(1L, "TestCert", null, null, null, false);
        CertificateService.Certificate mockCert = mock(CertificateService.Certificate.class);

        when(resultListRepository.findByResultListIdAndClassResultShortNameAndPersonId(any(), any(), any()))
                .thenReturn(rl);
        when(personRepository.findById(PersonId.of(1L))).thenReturn(Optional.of(person));
        when(eventRepository.findById(EventId.of(1L))).thenReturn(Optional.of(event));
        when(organisationRepository.findById(OrganisationId.of(1L))).thenReturn(Optional.of(org));
        when(eventCertificateService.getById(EventCertificateId.of(1L))).thenReturn(eventCert);
        when(certificateService.createCertificate(any(), any(), any(), any(), any(), any()))
                .thenReturn(mockCert);
        when(eventCertificateStatRepository.save(any())).thenReturn(null);

        var result = service.createCertificate(
                ResultListId.of(1L), ClassResultShortName.of("H21"), PersonId.of(1L));

        assertThat(result).isNotNull();
        verify(eventCertificateStatRepository).save(any());
    }
}
