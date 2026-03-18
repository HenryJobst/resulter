package de.jobst.resulter.application;

import de.jobst.resulter.adapter.driver.web.dto.EventDto;
import de.jobst.resulter.adapter.driver.web.mapper.EventMapper;
import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.EventQueryService;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.application.port.ResultListService;
import de.jobst.resulter.application.port.SplitTimeListRepository;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.EventId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.ResultList;
import de.jobst.resulter.domain.ResultListId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EventQueryServiceImpl implements EventQueryService {

    private final EventService eventService;
    private final OrganisationService organisationService;
    private final EventCertificateService eventCertificateService;
    private final ResultListService resultListService;
    private final SplitTimeListRepository splitTimeListRepository;

    public EventQueryServiceImpl(
            EventService eventService,
            OrganisationService organisationService,
            EventCertificateService eventCertificateService,
            ResultListService resultListService,
            SplitTimeListRepository splitTimeListRepository) {
        this.eventService = eventService;
        this.organisationService = organisationService;
        this.eventCertificateService = eventCertificateService;
        this.resultListService = resultListService;
        this.splitTimeListRepository = splitTimeListRepository;
    }

    @Override
    public List<EventDto> findAllAsDto() {
        return toDtos(eventService.findAll());
    }

    @Override
    public Page<EventDto> findAllAsDto(String filter, Pageable pageable) {
        Page<Event> page = eventService.findAll(filter, pageable);
        return new PageImpl<>(toDtos(page.getContent()), page.getPageable(), page.getTotalElements());
    }

    @Override
    public Optional<EventDto> findByIdAsDto(Long id) {
        return eventService.findById(EventId.of(id)).map(event -> toDtos(List.of(event)).getFirst());
    }

    private List<EventDto> toDtos(List<Event> events) {
        Map<Long, Boolean> hasSplitTimesMap = batchHasSplitTimes(events);
        Map<OrganisationId, Organisation> organisationMap = batchLoadOrganisations(events);
        Map<EventCertificateId, EventCertificate> eventCertificateMap = batchLoadCertificates(events);
        return EventMapper.toDtos(events, hasSplitTimesMap, organisationMap, eventCertificateMap);
    }

    private Map<Long, Boolean> batchHasSplitTimes(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }
        Set<EventId> eventIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        Map<EventId, List<ResultList>> resultListsByEvent = resultListService.findAllByEventIds(eventIds);
        Set<ResultListId> allResultListIds = resultListsByEvent.values().stream()
                .flatMap(List::stream)
                .map(ResultList::getId)
                .collect(Collectors.toSet());
        Set<ResultListId> resultListIdsWithSplitTimes = splitTimeListRepository.existsByResultListIds(allResultListIds);
        return events.stream().collect(Collectors.toMap(
                event -> event.getId().value(),
                event -> resultListsByEvent.getOrDefault(event.getId(), List.of()).stream()
                        .anyMatch(resultList -> resultListIdsWithSplitTimes.contains(resultList.getId()))));
    }

    private Map<OrganisationId, Organisation> batchLoadOrganisations(List<Event> events) {
        Set<OrganisationId> orgIds = events.stream()
                .flatMap(e -> e.getOrganisationIds().stream())
                .collect(Collectors.toSet());
        return organisationService.findAllByIdAsMap(orgIds);
    }

    private Map<EventCertificateId, EventCertificate> batchLoadCertificates(List<Event> events) {
        Set<EventCertificateId> certIds = events.stream()
                .map(Event::getCertificate)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toSet());
        return eventCertificateService.findAllByIdAsMap(certIds);
    }
}
