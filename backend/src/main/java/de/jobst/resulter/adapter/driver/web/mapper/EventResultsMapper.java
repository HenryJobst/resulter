package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.EventResultsDto;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.ResultList;
import java.util.Collection;

public class EventResultsMapper {

    public static EventResultsDto toDto(Event event, boolean eventHasCup, Collection<ResultList> resultLists) {
        return new EventResultsDto(resultLists.stream()
                .sorted()
                .map(x -> ResultListMapper.toDto(x, event, resultLists.size(), resultLists, eventHasCup))
                .toList());
    }
}
