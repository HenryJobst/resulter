package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.adapter.driver.web.mapper.EventMapper;
import de.jobst.resulter.adapter.driver.web.mapper.RaceMapper;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.Race;
import de.jobst.resulter.domain.ResultList;

public record ResultListKeyDto(Long id, EventKeyDto event, RaceDto race) {

    public static ResultListKeyDto from(ResultList resultList, Event event, Race race) {
        assert resultList.getClassResults() != null;
        return new ResultListKeyDto(
                resultList.getId().value(), EventMapper.toKeyDto(event), RaceMapper.toDtoStatic(race));
    }
}
