package de.jobst.resulter.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.jobst.resulter.adapter.driver.web.dto.EventDto;
import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.application.port.ResultListService;
import de.jobst.resulter.application.port.SplitTimeListRepository;
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
    private SplitTimeListRepository splitTimeListRepository;
    private EventQueryServiceImpl queryService;

    @BeforeEach
    void setUp() {
        eventService = mock(EventService.class);
        organisationService = mock(OrganisationService.class);
        eventCertificateService = mock(EventCertificateService.class);
        resultListService = mock(ResultListService.class);
        splitTimeListRepository = mock(SplitTimeListRepository.class);

        queryService = new EventQueryServiceImpl(
                eventService,
                organisationService,
                eventCertificateService,
                resultListService,
                splitTimeListRepository);
    }

    @Test
    void findAllAsDto_shouldReturnDtosWithCorrectFields() {
        OrganisationId orgId = OrganisationId.of(10L);
        EventCertificateId certId = EventCertificateId.of(99L);
        Event event = Event.of(1L, "Test Event", null, null, Set.of(orgId), null, certId, Discipline.LONG, false);

        when(eventService.findAll()).thenReturn(List.of(event));
        when(resultListService.findAllByEventIds(Set.of(event.getId()))).thenReturn(Map.of());
        when(organisationService.findAllByIdAsMap(Set.of(orgId)))
                .thenReturn(Map.of(orgId, Organisation.of(orgId.value(), "Org A", "A")));
        when(eventCertificateService.findAllByIdAsMap(Set.of(certId)))
                .thenReturn(Map.of(certId, EventCertificate.of(certId.value(), "Cert", event.getId(), "{}", null, true)));

        List<EventDto> dtos = queryService.findAllAsDto();

        assertThat(dtos).hasSize(1);
        EventDto dto = dtos.getFirst();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Test Event");
        assertThat(dto.hasSplitTimes()).isFalse();
        assertThat(dto.organisations()).hasSize(1);
        assertThat(dto.organisations().getFirst().name()).isEqualTo("Org A");
        assertThat(dto.certificate()).isNotNull();
        assertThat(dto.certificate().id()).isEqualTo(certId.value());
    }

    @Test
    void findAllAsDto_shouldPropagateSplitTimesFlag() {
        Event event1 = Event.of(1L, "With SplitTimes", null, null, Set.of(), null, Discipline.getDefault(), false);
        Event event2 = Event.of(2L, "Without SplitTimes", null, null, Set.of(), null, Discipline.getDefault(), false);
        ResultList rl1 = new ResultList(ResultListId.of(11L), event1.getId(), RaceId.of(1L), null, null, null, null);

        when(eventService.findAll()).thenReturn(List.of(event1, event2));
        when(resultListService.findAllByEventIds(Set.of(event1.getId(), event2.getId())))
                .thenReturn(Map.of(event1.getId(), List.of(rl1)));
        when(splitTimeListRepository.existsByResultListIds(Set.of(rl1.getId())))
                .thenReturn(Set.of(rl1.getId()));
        when(organisationService.findAllByIdAsMap(Set.of())).thenReturn(Map.of());
        when(eventCertificateService.findAllByIdAsMap(Set.of())).thenReturn(Map.of());

        List<EventDto> dtos = queryService.findAllAsDto();

        assertThat(dtos).hasSize(2);
        Map<Long, Boolean> splitTimesById = Map.of(
                dtos.get(0).id(), dtos.get(0).hasSplitTimes(),
                dtos.get(1).id(), dtos.get(1).hasSplitTimes());
        assertThat(splitTimesById).containsEntry(1L, true).containsEntry(2L, false);
    }

    @Test
    void findAllAsDto_shouldReturnEmptyListAndSkipBatchLoading() {
        when(eventService.findAll()).thenReturn(List.of());

        List<EventDto> dtos = queryService.findAllAsDto();

        assertThat(dtos).isEmpty();
        verify(resultListService, never()).findAllByEventIds(Set.of());
    }

    @Test
    void findByIdAsDto_shouldReturnPopulatedDto() {
        Event event = Event.of(5L, "Single Event", null, null, Set.of(), null, Discipline.getDefault(), false);

        when(eventService.findById(EventId.of(5L))).thenReturn(Optional.of(event));
        when(resultListService.findAllByEventIds(Set.of(event.getId()))).thenReturn(Map.of());
        when(organisationService.findAllByIdAsMap(Set.of())).thenReturn(Map.of());
        when(eventCertificateService.findAllByIdAsMap(Set.of())).thenReturn(Map.of());

        Optional<EventDto> dto = queryService.findByIdAsDto(5L);

        assertThat(dto).isPresent();
        assertThat(dto.get().id()).isEqualTo(5L);
        assertThat(dto.get().name()).isEqualTo("Single Event");
    }

    @Test
    void findByIdAsDto_shouldReturnEmptyForUnknownId() {
        when(eventService.findById(EventId.of(999L))).thenReturn(Optional.empty());

        Optional<EventDto> dto = queryService.findByIdAsDto(999L);

        assertThat(dto).isEmpty();
    }

    @Test
    void findAllAsDto_pageable_shouldReturnPagedDtos() {
        Event event = Event.of(7L, "Paged Event", null, null, Set.of(), null, Discipline.getDefault(), false);
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Event> page = new PageImpl<>(List.of(event), pageable, 1L);

        when(eventService.findAll(null, pageable)).thenReturn(page);
        when(resultListService.findAllByEventIds(Set.of(event.getId()))).thenReturn(Map.of());
        when(organisationService.findAllByIdAsMap(Set.of())).thenReturn(Map.of());
        when(eventCertificateService.findAllByIdAsMap(Set.of())).thenReturn(Map.of());

        Page<EventDto> result = queryService.findAllAsDto(null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getContent().getFirst().id()).isEqualTo(7L);
    }
}
