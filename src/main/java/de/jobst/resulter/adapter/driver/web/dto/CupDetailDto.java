package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.EventId;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

public record CupDetailDto(Long id, String name, CupTypeDto type, List<EventId> events) {

    static public CupDetailDto from(Cup cup) {
        return new CupDetailDto(ObjectUtils.isNotEmpty(cup.getId()) ? cup.getId().value() : 0,
            cup.getName().value(),
            CupTypeDto.from(cup.getType()),
            cup.getEventIds().stream().toList());
    }
}
