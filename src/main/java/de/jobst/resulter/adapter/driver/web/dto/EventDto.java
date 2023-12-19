package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Event;
import org.apache.commons.lang3.ObjectUtils;

public record EventDto(Long id,
                       String name,
                       String startTime,
                       Integer classes,
                       Long participants) {
    static public EventDto from(Event event) {
        return new EventDto(
                ObjectUtils.isNotEmpty(event.getId()) ?
                        event.getId().value() : 0,
                event.getName().value(),
                ObjectUtils.isNotEmpty(event.getStartTime()) && ObjectUtils.isNotEmpty(event.getStartTime().value()) ?
                        event.getStartTime().value().toString() : null,
                ObjectUtils.isNotEmpty(event.getClassResults()) && event.getClassResults().isPresent() ?
                        event.getClassResults().get().value().size() :
                        0,
                event.getClassResults().isPresent() ?
                        event.getClassResults()
                                .get()
                                .value()
                                .stream()
                                .filter(it -> it.personResults().isPresent())
                                .mapToLong(it -> it.personResults().get().value().size())
                                .sum() : 0
        );
    }
}
