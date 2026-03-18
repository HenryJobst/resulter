package de.jobst.resulter.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.jobst.resulter.application.port.EventBatchResult;
import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.application.port.ResultListService;
import de.jobst.resulter.domain.Discipline;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.RaceId;
import de.jobst.resulter.domain.ResultList;
import de.jobst.resulter.domain.ResultListId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class EventQueryServiceImplTest {

    private EventService eventService;
    private OrganisationService organisationService;
    private EventCertificateService eventCertificateService;
    private ResultListService resultListService;
    private EventQueryServiceImpl queryService;

    @BeforeEach
    void setUp() {
        eventService = mock(EventService.class);
        organisationService = mock(OrganisationService.class);
        eventCertificateService = mock(EventCertificateService.class);
        resultListService = mock(ResultListService.class);

        queryService = new EventQueryServiceImpl(
                eventService,
                organisationService,
                eventCertificateService,
                resultListService);
    }

    @Test
    void findAll_shouldReturnBatchResultWithCorrectData() {
        OrganisationId orgId = OrganisationId.of(10L);
        EventCertificateId certId = EventCertificateId.of(99L);
        Event event = Event.of(1L, "Test Event", null, null, Set.of(orgId), null, certId, Discipline.LONG, false);

        when(eventService.findAll()).thenReturn(List.of(event));
        when(resultListService.findAllByEventIds(Set.of(event.getId()))).thenReturn(Map.of());
        when(organisationService.findAllByIdAsMap(Set.of(orgId)))
                .thenReturn(Map.of(orgId, Organisation.of(orgId.value(), "Org A", "A")));
        when(eventCertificateService.findAllByIdAsMap(Set.of(certId)))
                .thenReturn(Map.of(certId, EventCertificate.of(certId.value(), "Cert", event.getId(), "{}", null, true)));

        EventBatchResult result = queryService.findAll();

        assertThat(result.events()).hasSize(1);
        assertThat(result.events().getFirst().getId().value()).isEqualTo(1L);
        assertThat(result.hasSplitTimesMap()).containsEntry(1L, false);
        assertThat(result.organisationMap()).containsKey(orgId);
        assertThat(result.certificateMap()).containsKey(certId);
    }

    @Test
    void findAll_shouldPropagateSplitTimesFlag() {
        Event event1 = Event.of(1L, "With SplitTimes", null, null, Set.of(), null, Discipline.getDefault(), false);
        Event event2 = Event.of(2L, "Without SplitTimes", null, null, Set.of(), null, Discipline.getDefault(), false);
        ResultList rl1 = new ResultList(ResultListId.of(11L), event1.getId(), RaceId.of(1L), null, null, null, null);

        when(eventService.findAll()).thenReturn(List.of(event1, event2));
        when(resultListService.findAllByEventIds(Set.of(event1.getId(), event2.getId())))
                .thenReturn(Map.of(event1.getId(), List.of(rl1)));
        when(resultListService.findResultListIdsWithSplitTimes(Set.of(rl1.getId())))
                .thenReturn(Set.of(rl1.getId()));
        when(organisationService.findAllByIdAsMap(Set.of())).thenReturn(Map.of());
        when(eventCertificateService.findAllByIdAsMap(Set.of())).thenReturn(Map.of());

        EventBatchResult result = queryService.findAll();

        assertThat(result.events()).hasSize(2);
        assertThat(result.hasSplitTimesMap()).containsEntry(1L, true).containsEntry(2L, false);
    }

    @Test
    void findAll_shouldReturnEmptyBatchResultAndSkipBatchLoading() {
        when(eventService.findAll()).thenReturn(List.of());

        EventBatchResult result = queryService.findAll();

        assertThat(result.events()).isEmpty();
        verify(resultListService, never()).findAllByEventIds(Set.of());
    }

    @Test
    void findById_shouldReturnBatchResultWithSingleEvent() {
        Event event = Event.of(5L, "Single Event", null, null, Set.of(), null, Discipline.getDefault(), false);

        when(eventService.findById(EventId.of(5L))).thenReturn(Optional.of(event));
        when(resultListService.findAllByEventIds(Set.of(event.getId()))).thenReturn(Map.of());
        when(organisationService.findAllByIdAsMap(Set.of())).thenReturn(Map.of());
        when(eventCertificateService.findAllByIdAsMap(Set.of())).thenReturn(Map.of());

        Optional<EventBatchResult> result = queryService.findById(5L);

        assertThat(result).isPresent();
        assertThat(result.get().events()).hasSize(1);
        assertThat(result.get().events().getFirst().getId().value()).isEqualTo(5L);
    }

    @Test
    void findById_shouldReturnEmptyForUnknownId() {
        when(eventService.findById(EventId.of(999L))).thenReturn(Optional.empty());

        Optional<EventBatchResult> result = queryService.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_pageable_shouldReturnBatchResultWithPageMetadata() {
        Event event = Event.of(7L, "Paged Event", null, null, Set.of(), null, Discipline.getDefault(), false);
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Event> page = new PageImpl<>(List.of(event), pageable, 1L);

        when(eventService.findAll(null, pageable)).thenReturn(page);
        when(resultListService.findAllByEventIds(Set.of(event.getId()))).thenReturn(Map.of());
        when(organisationService.findAllByIdAsMap(Set.of())).thenReturn(Map.of());
        when(eventCertificateService.findAllByIdAsMap(Set.of())).thenReturn(Map.of());

        EventBatchResult result = queryService.findAll(null, pageable);

        assertThat(result.events()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.events().getFirst().getId().value()).isEqualTo(7L);
    }
}
