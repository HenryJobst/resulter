package de.jobst.resulter.adapter.driver.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.jobst.resulter.application.EventQueryServiceImpl;
import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.application.port.ResultListService;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.Discipline;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.RaceId;
import de.jobst.resulter.domain.ResultList;
import de.jobst.resulter.domain.ResultListId;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventControllerBatchHasSplitTimesTest {

    private ResultListService resultListService;
    private SplitTimeListRepository splitTimeListRepository;
    private EventQueryServiceImpl eventQueryService;

    @BeforeEach
    void setUp() {
        EventService eventService = mock(EventService.class);
        OrganisationService organisationService = mock(OrganisationService.class);
        EventCertificateService eventCertificateService = mock(EventCertificateService.class);
        resultListService = mock(ResultListService.class);
        splitTimeListRepository = mock(SplitTimeListRepository.class);

        eventQueryService = new EventQueryServiceImpl(
                eventService,
                organisationService,
                eventCertificateService,
                resultListService,
                splitTimeListRepository);
    }

    @Test
    void batchHasSplitTimes_shouldUseBatchRepositoryCalls() throws Exception {
        Event event1 = Event.of(1L, "Event 1", null, null, Set.of(), null, Discipline.getDefault(), false);
        Event event2 = Event.of(2L, "Event 2", null, null, Set.of(), null, Discipline.getDefault(), false);

        ResultList resultList1 = new ResultList(ResultListId.of(11L), event1.getId(), RaceId.of(100L), null, null, null, null);
        ResultList resultList2 = new ResultList(ResultListId.of(22L), event2.getId(), RaceId.of(200L), null, null, null, null);

        when(resultListService.findAllByEventIds(Set.of(event1.getId(), event2.getId())))
                .thenReturn(Map.of(
                        event1.getId(), List.of(resultList1),
                        event2.getId(), List.of(resultList2)));
        when(splitTimeListRepository.existsByResultListIds(Set.of(resultList1.getId(), resultList2.getId())))
                .thenReturn(Set.of(resultList2.getId()));

        Method method = EventQueryServiceImpl.class.getDeclaredMethod("batchHasSplitTimes", List.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<Long, Boolean> result = (Map<Long, Boolean>) method.invoke(eventQueryService, List.of(event1, event2));

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                event1.getId().value(), false,
                event2.getId().value(), true));
        verify(resultListService).findAllByEventIds(Set.of(event1.getId(), event2.getId()));
        verify(splitTimeListRepository).existsByResultListIds(Set.of(resultList1.getId(), resultList2.getId()));
    }

    @Test
    void batchHasSplitTimes_shouldReturnEmptyMapForEmptyInput() throws Exception {
        Method method = EventQueryServiceImpl.class.getDeclaredMethod("batchHasSplitTimes", List.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<Long, Boolean> result = (Map<Long, Boolean>) method.invoke(eventQueryService, List.of());

        assertThat(result).isEmpty();
        verify(resultListService, never()).findAllByEventIds(any());
        verify(splitTimeListRepository, never()).existsByResultListIds(any());
    }
}
