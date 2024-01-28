package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.EventStatus;

public record EventStatusDto(String id) {

    static public EventStatusDto from(EventStatus eventStatus) {
        return new EventStatusDto(eventStatus.value());
    }
}
