package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.DisciplineDto;
import de.jobst.resulter.adapter.driver.web.dto.EventCertificateKeyDto;
import de.jobst.resulter.adapter.driver.web.dto.EventDto;
import de.jobst.resulter.adapter.driver.web.dto.EventKeyDto;
import de.jobst.resulter.adapter.driver.web.dto.EventStatusDto;
import de.jobst.resulter.application.port.EventCertificateService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventStatus;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class EventMapper {

    private final OrganisationService organisationService;
    private final EventCertificateService eventCertificateService;

    public EventMapper(
            OrganisationService organisationService,
            EventCertificateService eventCertificateService) {
        this.organisationService = organisationService;
        this.eventCertificateService = eventCertificateService;
    }

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
                event.isAggregatedScore()
        );
    }

    public static EventKeyDto toKeyDto(Event event) {
        return new EventKeyDto(
                ObjectUtils.isNotEmpty(event.getId()) ? event.getId().value() : 0,
                event.getName().value(),
                Objects.nonNull(event.getStartTime()) ? event.getStartTime().value() : null);
    }
}
