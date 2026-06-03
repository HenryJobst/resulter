package de.jobst.resulter.application;

import de.jobst.resulter.application.port.*;
import de.jobst.resulter.domain.*;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CupQueryServiceImplTest {

    @Mock CupService cupService;
    @Mock EventService eventService;
    @Mock OrganisationService organisationService;
    @Mock EventCertificateService eventCertificateService;
    @Mock CountryService countryService;
    @Mock ResultListService resultListService;

    @InjectMocks
    CupQueryServiceImpl service;

    private static Cup cup(long id) {
        return Cup.of(id, "Cup", CupType.ADD, Year.of(2024), List.of());
    }

    @Test
    void findAll_returnsAllCups() {
        when(cupService.findAll()).thenReturn(List.of(cup(1L)));
        when(eventService.findAllById(any())).thenReturn(List.of());
        CupBatchResult result = service.findAll();
        assertThat(result.cups()).hasSize(1);
    }

    @Test
    void findAll_withNoEvents_returnsEmptyEventMap() {
        when(cupService.findAll()).thenReturn(List.of());
        when(eventService.findAllById(any())).thenReturn(List.of());
        CupBatchResult result = service.findAll();
        assertThat(result.cups()).isEmpty();
    }

    @Test
    void findAll_paged_returnsPage() {
        when(cupService.findAll(any(), any()))
                .thenReturn(new PageImpl<>(List.of(cup(1L))));
        when(eventService.findAllById(any())).thenReturn(List.of());
        CupBatchResult result = service.findAll(null, PageRequest.of(0, 10));
        assertThat(result.cups()).hasSize(1);
    }

    @Test
    void findById_returnsPresent_whenFound() {
        Cup c = cup(1L);
        when(cupService.findById(CupId.of(1L))).thenReturn(Optional.of(c));
        when(eventService.findAllById(any())).thenReturn(List.of());
        Optional<CupBatchResult> result = service.findById(1L);
        assertThat(result).isPresent();
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        when(cupService.findById(CupId.of(99L))).thenReturn(Optional.empty());
        Optional<CupBatchResult> result = service.findById(99L);
        assertThat(result).isEmpty();
    }

    @Test
    void batchHasSplitTimes_returnsEmpty_forEmptyEventList() {
        Map<EventId, Boolean> result = service.batchHasSplitTimes(List.of());
        assertThat(result).isEmpty();
    }

    @Test
    void batchHasSplitTimes_returnsFalse_whenNoSplitTimes() {
        Event event = Event.of(1L, "Sprint");
        when(resultListService.findAllByEventIds(any())).thenReturn(Map.of());
        when(resultListService.findResultListIdsWithSplitTimes(any())).thenReturn(Set.of());
        Map<EventId, Boolean> result = service.batchHasSplitTimes(List.of(event));
        assertThat(result).containsKey(EventId.of(1L));
        assertThat(result.get(EventId.of(1L))).isFalse();
    }

    @Test
    void batchHasSplitTimes_returnsTrue_whenSplitTimesExist() {
        Event event = Event.of(1L, "Sprint");
        ResultList rl = new ResultList(ResultListId.of(10L), EventId.of(1L), RaceId.empty(), null, null, null, null);
        when(resultListService.findAllByEventIds(any()))
                .thenReturn(Map.of(EventId.of(1L), List.of(rl)));
        when(resultListService.findResultListIdsWithSplitTimes(any()))
                .thenReturn(Set.of(ResultListId.of(10L)));
        Map<EventId, Boolean> result = service.batchHasSplitTimes(List.of(event));
        assertThat(result.get(EventId.of(1L))).isTrue();
    }

    @Test
    void findCupDetailed_returnsEmpty_whenResourceNotFoundException() {
        when(cupService.getCupDetailed(CupId.of(99L)))
                .thenThrow(new de.jobst.resulter.domain.util.ResourceNotFoundException("not found"));

        Optional<de.jobst.resulter.application.port.CupDetailedBatchResult> result = service.findCupDetailed(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void findCupDetailed_returnsResult_whenFound() {
        de.jobst.resulter.domain.aggregations.CupStatistics stats =
                new de.jobst.resulter.domain.aggregations.CupStatistics(
                        new de.jobst.resulter.domain.aggregations.CupOverallStatistics(
                                0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0),
                        List.of());
        de.jobst.resulter.domain.aggregations.CupDetailed cupDetailed =
                new de.jobst.resulter.domain.aggregations.CupDetailed(
                        cup(1L), List.of(), List.of(), Map.of(), stats);

        when(cupService.getCupDetailed(CupId.of(1L))).thenReturn(cupDetailed);
        when(eventService.findAllById(any())).thenReturn(List.of());
        when(organisationService.findAllByIdAsMap(any())).thenReturn(Map.of());
        when(eventCertificateService.findAllByIdAsMap(any())).thenReturn(Map.of());
        when(countryService.batchLoadForOrganisations(any())).thenReturn(Map.of());
        when(organisationService.batchLoadChildOrganisations(any())).thenReturn(Map.of());

        Optional<de.jobst.resulter.application.port.CupDetailedBatchResult> result = service.findCupDetailed(1L);

        assertThat(result).isPresent();
    }
}
