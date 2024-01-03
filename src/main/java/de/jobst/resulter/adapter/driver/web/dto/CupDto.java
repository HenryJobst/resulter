package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Cup;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

public record CupDto(Long id,
                     String name,
                     String type,
                     List<EventDto> events) {
    static public CupDto from(Cup cup) {
        return new CupDto(
                ObjectUtils.isNotEmpty(cup.getId()) ?
                        cup.getId().value() : 0,
                cup.getName().value(),
                cup.getType().value(),
                cup.getEvents().isLoaded() ?
                        cup.getEvents().get().value().stream().map(EventDto::from).sorted().toList() :
                        new ArrayList<>()
        );
    }
}
