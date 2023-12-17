package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Event;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDate;

public record EventDto(Long id, String name,
                       LocalDate startTime,
                       Integer classes,
                       Long participants) {
    static public EventDto from(Event event) {
        return new EventDto(
                ObjectUtils.isNotEmpty(event.getId()) ?
                        event.getId().value() : 0,
                event.getName().value(),
                ObjectUtils.isNotEmpty(event.getStartTime()) ?
                        event.getStartTime().value().toLocalDate() : null,
                ObjectUtils.isNotEmpty(event.getClassResults()) ? event.getClassResults().value().size() : 0,
                event.getClassResults().value().stream().mapToLong(it -> it.personResults().value().size()).sum()
        );
    }
}
