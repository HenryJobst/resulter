package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.OrganisationId;
import org.apache.commons.lang3.ObjectUtils;

public record EventDto(Long id, String name, String startTime, Long[] organisations) {

    static public EventDto from(Event event) {
        return new EventDto(ObjectUtils.isNotEmpty(event.getId()) ? event.getId().value() : 0,
            event.getName().value(),
            ObjectUtils.isNotEmpty(event.getStartTime()) && ObjectUtils.isNotEmpty(event.getStartTime().value()) ?
            event.getStartTime().value().toString() :
            null,
            event.getOrganisationIds().stream().map(OrganisationId::value).toArray(Long[]::new));
    }
}
