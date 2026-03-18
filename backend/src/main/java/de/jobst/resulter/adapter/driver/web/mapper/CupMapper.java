package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.CupDto;
import de.jobst.resulter.adapter.driver.web.dto.CupTypeDto;
import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;

public class CupMapper {

    public static CupDto toDto(Cup cup, Map<EventId, Event> eventMap) {
        return new CupDto(
                ObjectUtils.isNotEmpty(cup.getId()) ? cup.getId().value() : 0,
                cup.getName().value(),
                CupTypeDto.from(cup.getType()),
                cup.getYear().getValue(),
                cup.getEventIds().stream()
                        .map(eventMap::get)
                        .filter(java.util.Objects::nonNull)
                        .map(EventMapper::toKeyDto)
                        .sorted()
                        .toList());
    }

    public static List<CupDto> toDtos(List<Cup> cups, Map<EventId, Event> eventMap) {
        return cups.stream().map(cup -> toDto(cup, eventMap)).toList();
    }
}
