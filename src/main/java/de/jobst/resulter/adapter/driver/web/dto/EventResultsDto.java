package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.ResultListService;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.ResultList;

import java.util.Collection;

public record EventResultsDto(Collection<ResultListDto> resultLists) {

    static public EventResultsDto from(Event event, ResultListService resultListService) {
        Collection<ResultList> resultLists = resultListService.findByEventId(event.getId());
        return new EventResultsDto(resultLists.stream().sorted().map(ResultListDto::from).toList());
    }
}
