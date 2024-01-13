package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.EventId;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collection;

public record CupDto(Long id, String name, CupTypeDto type, Collection<Long> eventIds) {

    static public CupDto from(Cup cup) {
        return new CupDto(ObjectUtils.isNotEmpty(cup.getId()) ? cup.getId().value() : 0,
            cup.getName().value(),
            CupTypeDto.from(cup.getType()),
            cup.getEventIds().stream().map(EventId::value).toList());
    }
}
