package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.CupDetailed;
import de.jobst.resulter.domain.EventId;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

public record CupDetailedDto(Long id, String name, CupTypeDto type,
                             List<EventId> events,
                             List<EventRacesCupScoreDto> eventRacesCupScoreDto) {

    static public CupDetailedDto from(CupDetailed cup) {
        return new CupDetailedDto(
            ObjectUtils.isNotEmpty(cup.getId()) ? cup.getId().value() : 0,
            cup.getName().value(),
            CupTypeDto.from(cup.getType()),
            cup.getEventIds().stream().toList(),
            cup.getEventRacesCupScore().stream().map(EventRacesCupScoreDto::from).toList()
            );
    }
}
