package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Cup;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

public record CupDto(Long id,
                     String name,
                     CupTypeDto type,
                     List<EventDto> events) {
    static public CupDto from(Cup cup) {
        return new CupDto(
                ObjectUtils.isNotEmpty(cup.getId()) ?
                        cup.getId().value() : 0,
                cup.getName().value(),
                CupTypeDto.from(cup.getType()),
                cup.getEvents().isLoaded() ?
                        cup.getEvents().get().value().stream().sorted().map(EventDto::from).toList() :
                        new ArrayList<>()
        );
    }
}
