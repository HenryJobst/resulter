package de.jobst.resulter.adapter.driver.web.dto;

import java.util.List;
import org.springframework.data.domain.Sort;

public record CupDto(Long id, String name, CupTypeDto type, Integer year, List<EventKeyDto> events) {

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
