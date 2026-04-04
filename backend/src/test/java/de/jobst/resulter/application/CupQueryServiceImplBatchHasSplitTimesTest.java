package de.jobst.resulter.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.application.port.CupService;
import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.application.port.ResultListService;
import de.jobst.resulter.domain.Discipline;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.RaceId;
import de.jobst.resulter.domain.ResultList;
import de.jobst.resulter.domain.ResultListId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CupQueryServiceImplBatchHasSplitTimesTest {

    private ResultListService resultListService;
    private CupQueryServiceImpl cupQueryServiceImpl;

    @BeforeEach
    void setUp() {
        resultListService = mock(ResultListService.class);
        cupQueryServiceImpl = new CupQueryServiceImpl(
                mock(CupService.class),
                mock(EventService.class),
                mock(OrganisationService.class),
                mock(EventCertificateService.class),
                mock(CountryService.class),
                resultListService);
    }

    @Test
    void batchHasSplitTimes_shouldUseBatchServiceCalls() {
        Event event1 = Event.of(101L, "Event 101", null, null, Set.of(), null, Discipline.getDefault(), false);
        Event event2 = Event.of(202L, "Event 202", null, null, Set.of(), null, Discipline.getDefault(), false);

        ResultList resultList1 =
                new ResultList(ResultListId.of(111L), event1.getId(), RaceId.of(1L), null, null, null, null);
        ResultList resultList2 =
                new ResultList(ResultListId.of(222L), event2.getId(), RaceId.of(2L), null, null, null, null);

        when(resultListService.findAllByEventIds(Set.of(event1.getId(), event2.getId())))
                .thenReturn(Map.of(
                        event1.getId(), List.of(resultList1),
                        event2.getId(), List.of(resultList2)));
        when(resultListService.findResultListIdsWithSplitTimes(
                        Set.of(resultList1.getId(), resultList2.getId())))
                .thenReturn(Set.of(resultList1.getId()));

        Map<EventId, Boolean> result = cupQueryServiceImpl.batchHasSplitTimes(List.of(event1, event2));

        assertThat(result)
                .containsExactlyInAnyOrderEntriesOf(
                        Map.of(event1.getId(), true, event2.getId(), false));
        verify(resultListService).findAllByEventIds(Set.of(event1.getId(), event2.getId()));
        verify(resultListService)
                .findResultListIdsWithSplitTimes(Set.of(resultList1.getId(), resultList2.getId()));
    }

    @Test
    void batchHasSplitTimes_shouldReturnEmptyMapForEmptyInput() {
        Map<EventId, Boolean> result = cupQueryServiceImpl.batchHasSplitTimes(List.of());

        assertThat(result).isEmpty();
        verify(resultListService, never()).findAllByEventIds(any());
        verify(resultListService, never()).findResultListIdsWithSplitTimes(any());
    }
}
