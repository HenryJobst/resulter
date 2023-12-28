package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Event;

import java.util.ArrayList;
import java.util.Collection;

public record EventResultsDto(Collection<ClassResultDto> classResultDtos) {
    static public EventResultsDto from(Event event) {
        return new EventResultsDto(
                event.getClassResults().isLoaded() ?
                        event.getClassResults()
                                .get()
                                .value()
                                .stream()
                                .filter(it -> it.getPersonResults().isLoaded())
                                .sorted()
                                .map(ClassResultDto::from)
                                .toList() : new ArrayList<>());
    }
}
