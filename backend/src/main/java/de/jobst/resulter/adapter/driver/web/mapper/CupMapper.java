package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.CupDto;
import de.jobst.resulter.adapter.driver.web.dto.CupTypeDto;
import de.jobst.resulter.adapter.driver.web.dto.EventKeyDto;
import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CupMapper {

    private final EventService eventService;

    public CupMapper(EventService eventService) {
        this.eventService = eventService;
    }

    public CupDto toDto(Cup cup) {
        return new CupDto(
                ObjectUtils.isNotEmpty(cup.getId()) ? cup.getId().value() : 0,
                cup.getName().value(),
                CupTypeDto.from(cup.getType()),
                cup.getYear().getValue(),
                cup.getEventIds().stream()
                        .map(x -> EventMapper.toKeyDto(eventService.getById(x)))
                        .sorted()
                        .toList());
    }

    public CupDto toDto(Cup cup, Map<EventId, Event> eventMap) {
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

    public List<CupDto> toDtos(List<Cup> cups) {
        // Batch-load all Events for all Cups
        List<EventId> allEventIds = cups.stream()
                .flatMap(cup -> cup.getEventIds().stream())
                .distinct()
                .toList();
        Map<EventId, Event> eventMap = eventService.findAllById(allEventIds).stream()
                .collect(Collectors.toMap(Event::getId, event -> event));

        return cups.stream()
                .map(cup -> toDto(cup, eventMap))
                .toList();
    }
}
