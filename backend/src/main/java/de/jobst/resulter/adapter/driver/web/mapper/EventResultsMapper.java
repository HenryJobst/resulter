package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.EventResultsDto;
import de.jobst.resulter.application.port.ResultListService;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.ResultList;
import java.util.Collection;
import org.springframework.stereotype.Component;

@Component
public class EventResultsMapper {

    private final ResultListService resultListService;

    public EventResultsMapper(ResultListService resultListService) {
        this.resultListService = resultListService;
    }

    public EventResultsDto toDto(Event event, boolean eventHasCup) {
        Collection<ResultList> resultLists = resultListService.findByEventId(event.getId());
        return new EventResultsDto(resultLists.stream()
                .sorted()
                .map(x -> ResultListMapper.toDto(x, event, resultLists.size(), resultLists, eventHasCup))
                .toList());
    }
}
