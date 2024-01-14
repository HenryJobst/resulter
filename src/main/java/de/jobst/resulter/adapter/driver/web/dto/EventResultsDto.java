package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.port.ResultListRepository;
import de.jobst.resulter.domain.Event;

import java.util.Collection;
import java.util.Objects;

public record EventResultsDto(Collection<ResultListDto> resultListDtos) {

    static public EventResultsDto from(Event event, ResultListRepository resultListRepository) {
        return new EventResultsDto(event.getResultListIds()
            .stream()
            .sorted()
            .map(x -> ResultListDto.from(Objects.requireNonNull(resultListRepository.findById(x).orElse(null))))
            .toList());
    }
}
