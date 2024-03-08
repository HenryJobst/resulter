package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Event;
import org.apache.commons.lang3.ObjectUtils;

public record EventKeyDto(Long id, String name) {

    static public EventKeyDto from(Event event) {
        return new EventKeyDto(ObjectUtils.isNotEmpty(event.getId()) ? event.getId().value() : 0,
            event.getName().value());
    }

}
