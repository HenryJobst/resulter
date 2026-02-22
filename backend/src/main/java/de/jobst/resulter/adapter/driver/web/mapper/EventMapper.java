package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.DisciplineDto;
import de.jobst.resulter.adapter.driver.web.dto.EventCertificateKeyDto;
import de.jobst.resulter.adapter.driver.web.dto.EventDto;
import de.jobst.resulter.adapter.driver.web.dto.EventKeyDto;
import de.jobst.resulter.adapter.driver.web.dto.EventStatusDto;
import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.EventStatus;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    private final OrganisationService organisationService;
    private final EventCertificateService eventCertificateService;

    public EventMapper(OrganisationService organisationService, EventCertificateService eventCertificateService) {
        this.organisationService = organisationService;
        this.eventCertificateService = eventCertificateService;
    }

    /**
     * Convert single event to DTO. This method causes N+1 queries by loading organisations one by one.
     * @deprecated Use toDtos() for batch conversion to avoid N+1 queries.
     */
    @Deprecated(since = "4.6.2", forRemoval = true)
    public EventDto toDto(Event event, Boolean hasSplitTimes) {
        return new EventDto(
                ObjectUtils.isNotEmpty(event.getId()) ? event.getId().value() : 0,
                event.getName().value(),
                ObjectUtils.isNotEmpty(event.getStartTime())
                                && ObjectUtils.isNotEmpty(event.getStartTime().value())
                        ? event.getStartTime().value().toString()
                        : null,
                EventStatusDto.from(event.getEventState() != null ? event.getEventState() : EventStatus.getDefault()),
                event.getOrganisationIds().stream()
                        .map(x -> {
                            var organisation = organisationService.getById(x);
                            return OrganisationMapper.toKeyDto(organisation);
                        })
                        .toList(),
                ObjectUtils.isNotEmpty(event.getCertificate())
                        ? EventCertificateKeyDto.from(eventCertificateService.getById(event.getCertificate()))
                        : null,
                hasSplitTimes,
                DisciplineDto.from(event.getDiscipline()),
                event.isAggregatedScore());
    }

    /**
     * Convert single event to DTO using pre-loaded organisation map.
     * Avoids N+1 queries by using the provided organisation map.
     */
    public EventDto toDto(
            Event event,
            Map<OrganisationId, Organisation> organisationMap,
            Map<EventCertificateId, EventCertificate> eventCertificateMap,
            Boolean hasSplitTimes) {
        return new EventDto(
                ObjectUtils.isNotEmpty(event.getId()) ? event.getId().value() : 0,
                event.getName().value(),
                ObjectUtils.isNotEmpty(event.getStartTime())
                                && ObjectUtils.isNotEmpty(event.getStartTime().value())
                        ? event.getStartTime().value().toString()
                        : null,
                EventStatusDto.from(event.getEventState() != null ? event.getEventState() : EventStatus.getDefault()),
                event.getOrganisationIds().stream()
                        .map(orgId -> {
                            Organisation organisation = organisationMap.get(orgId);
                            return organisation != null ? OrganisationMapper.toKeyDto(organisation) : null;
                        })
                        .filter(Objects::nonNull)
                        .toList(),
                ObjectUtils.isNotEmpty(event.getCertificate())
                                && ObjectUtils.isNotEmpty(eventCertificateMap.get(event.getCertificate()))
                        ? EventCertificateKeyDto.from(eventCertificateMap.get(event.getCertificate()))
                        : null,
                hasSplitTimes,
                DisciplineDto.from(event.getDiscipline()),
                event.isAggregatedScore());
    }

    /**
     * Convert list of events to DTOs with batch loading to avoid N+1 queries.
     * Loads all organisations in a single query.
     */
    public List<EventDto> toDtos(List<Event> events, Map<Long, Boolean> hasSplitTimesMap) {
        // Batch load all organisations for all events
        Set<OrganisationId> allOrgIds = events.stream()
                .flatMap(event -> event.getOrganisationIds().stream())
                .collect(Collectors.toSet());
        Set<EventCertificateId> allCertificateIds = events.stream()
                .map(Event::getCertificate)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toSet());

        Map<OrganisationId, Organisation> organisationMap = organisationService.findAllByIdAsMap(allOrgIds);
        Map<EventCertificateId, EventCertificate> eventCertificateMap =
                eventCertificateService.findAllByIdAsMap(allCertificateIds);

        // Convert all events using the pre-loaded organisation map
        return events.stream()
                .map(event -> toDto(
                        event,
                        organisationMap,
                        eventCertificateMap,
                        hasSplitTimesMap.getOrDefault(
                                ObjectUtils.isNotEmpty(event.getId())
                                        ? event.getId().value()
                                        : 0,
                                false)))
                .toList();
    }

    public static EventKeyDto toKeyDto(Event event) {
        return new EventKeyDto(
                ObjectUtils.isNotEmpty(event.getId()) ? event.getId().value() : 0,
                event.getName().value(),
                Objects.nonNull(event.getStartTime()) ? event.getStartTime().value() : null);
    }
}
