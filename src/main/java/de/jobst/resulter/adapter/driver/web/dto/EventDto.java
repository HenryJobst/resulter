package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Event;
import org.apache.commons.lang3.ObjectUtils;

public record EventDto(Long id,
                       String name,
                       String startTime,
                       Integer classes,
                       Long participants,
                       Long[] organisations) {
    static public EventDto from(Event event) {
        return new EventDto(
                ObjectUtils.isNotEmpty(event.getId()) ?
                        event.getId().value() : 0,
                event.getName().value(),
                ObjectUtils.isNotEmpty(event.getStartTime()) && ObjectUtils.isNotEmpty(event.getStartTime().value()) ?
                        event.getStartTime().value().toString() : null,
                event.getClassResults().isLoaded() ? event.getClassResults().get().value().size() : 0,
                event.getClassResults().isLoaded() ?
                        event.getClassResults()
                                .get()
                                .value()
                                .stream()
                                .filter(it -> it.getPersonResults().isLoaded())
                                .mapToLong(it -> it.getPersonResults().get().value().size())
                                .sum() : 0,
                event.getOrganisations().isLoaded() ?
                        event.getOrganisations().get().value().stream()
                                .map(it -> it.getId().value())
                                .toArray(Long[]::new) : new Long[0]

        );
    }
}
