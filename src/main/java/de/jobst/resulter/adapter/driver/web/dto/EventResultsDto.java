package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Event;

import java.util.Collection;

public record EventResultsDto(Collection<ClassResultDto> classResultDtos) {

    static public EventResultsDto from(Event event) {
        return new EventResultsDto(event.getClassResults()
            .value()
            .stream()
            .sorted()
            .map(ClassResultDto::from)
            .toList());
    }
}
