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
}
