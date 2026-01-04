package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.port.ResultListService;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.ResultList;
import java.util.Collection;

public record EventResultsDto(Collection<ResultListDto> resultLists) {

    public static EventResultsDto from(Event event, ResultListService resultListService, boolean eventHasCup) {
        Collection<ResultList> resultLists = resultListService.findByEventId(event.getId());
        return new EventResultsDto(resultLists.stream()
                .sorted()
                .map(x -> ResultListDto.from(x, event, resultLists.size(), resultLists, eventHasCup))
                .toList());
    }
}
