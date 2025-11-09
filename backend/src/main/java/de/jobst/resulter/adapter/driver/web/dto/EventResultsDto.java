package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.port.RaceService;
import de.jobst.resulter.application.port.ResultListService;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.Race;
import de.jobst.resulter.domain.ResultList;
import java.util.Collection;

public record EventResultsDto(Collection<ResultListDto> resultLists) {

    public static EventResultsDto from(Event event, ResultListService resultListService, RaceService raceService) {
        Collection<ResultList> resultLists = resultListService.findByEventId(event.getId());
        return new EventResultsDto(resultLists.stream()
                .sorted()
                .map(x -> {
                    Race race = raceService.findById(x.getRaceId()).orElseThrow();
                    return ResultListDto.from(x, event, race, resultLists.size());
                })
                .toList());
    }
}
