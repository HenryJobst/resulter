package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.port.EventService;
import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.Event;
import de.jobst.resulter.domain.EventId;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

public record CupDto(Long id, String name, CupTypeDto type, Integer year, List<EventKeyDto> events) {

    public static CupDto from(Cup cup, EventService eventService) {
        return new CupDto(
                ObjectUtils.isNotEmpty(cup.getId()) ? cup.getId().value() : 0,
                cup.getName().value(),
                CupTypeDto.from(cup.getType()),
                cup.getYear().getValue(),
                cup.getEventIds().stream()
                        .map(x -> EventKeyDto.from(eventService.getById(x)))
                        .sorted()
                        .toList());
    }

    public static CupDto from(Cup cup, Map<EventId, Event> eventMap) {
        return new CupDto(
                ObjectUtils.isNotEmpty(cup.getId()) ? cup.getId().value() : 0,
                cup.getName().value(),
                CupTypeDto.from(cup.getType()),
                cup.getYear().getValue(),
                cup.getEventIds().stream()
                        .map(eventMap::get)
                        .filter(java.util.Objects::nonNull)
                        .map(EventKeyDto::from)
                        .sorted()
                        .toList());
    }

    public static String mapOrdersDtoToDomain(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id" -> "id.value";
            case "name" -> "name.value";
            default -> order.getProperty();
        };
    }

    public static String mapOrdersDomainToDto(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id.value" -> "id";
            case "name.value" -> "name";
            default -> order.getProperty();
        };
    }
}
