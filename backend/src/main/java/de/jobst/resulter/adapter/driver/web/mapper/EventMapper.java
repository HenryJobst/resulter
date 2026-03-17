package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.DisciplineDto;
import de.jobst.resulter.adapter.driver.web.dto.EventCertificateKeyDto;
import de.jobst.resulter.adapter.driver.web.dto.EventDto;
import de.jobst.resulter.adapter.driver.web.dto.EventKeyDto;
import de.jobst.resulter.adapter.driver.web.dto.EventStatusDto;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventCertificate;
import de.jobst.resulter.domain.EventCertificateId;
import de.jobst.resulter.domain.EventStatus;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.ObjectUtils;

public class EventMapper {

    private EventMapper() {}

    public static EventDto toDto(
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

    public static List<EventDto> toDtos(
            List<Event> events,
            Map<Long, Boolean> hasSplitTimesMap,
            Map<OrganisationId, Organisation> organisationMap,
            Map<EventCertificateId, EventCertificate> eventCertificateMap) {
        return events.stream()
                .map(event -> toDto(
                        event,
                        organisationMap,
                        eventCertificateMap,
                        hasSplitTimesMap.getOrDefault(
                                ObjectUtils.isNotEmpty(event.getId()) ? event.getId().value() : 0, false)))
                .toList();
    }

    public static EventKeyDto toKeyDto(Event event) {
        return new EventKeyDto(
                ObjectUtils.isNotEmpty(event.getId()) ? event.getId().value() : 0,
                event.getName().value(),
                Objects.nonNull(event.getStartTime()) ? event.getStartTime().value() : null);
    }
}
