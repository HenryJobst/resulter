package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.Race;
import de.jobst.resulter.domain.ResultList;

public record ResultListKeyDto(Long id, EventKeyDto event, RaceDto race) {

    static public ResultListKeyDto from(ResultList resultList, Event event, Race race) {
        assert resultList.getClassResults() != null;
        return new ResultListKeyDto(resultList.getId().value(), EventKeyDto.from(event), RaceDto.from(race));
    }
}
