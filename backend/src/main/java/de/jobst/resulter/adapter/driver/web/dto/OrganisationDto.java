package de.jobst.resulter.adapter.driver.web.dto;

import java.util.List;
import org.springframework.data.domain.Sort;

public record OrganisationDto(
        Long id,
        String name,
        String shortName,
        OrganisationTypeDto type,
        CountryKeyDto country,
        List<OrganisationKeyDto> childOrganisations) {

    public static String mapOrdersDtoToDomain(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id" -> "id.value";
            case "name" -> "name.value";
            case "shortName" -> "shortName.value";
            case "type" -> "type.id";
            case "country.name" -> "country.name.value";
            case "childOrganisationIds" -> "childOrganisationIds";
            default -> order.getProperty();
        };
    }

    public static String mapOrdersDomainToDto(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id.value" -> "id";
            case "name.value" -> "name";
            case "shortName.value" -> "shortName";
            case "type.id" -> "type";
            case "country.name.value" -> "country.name";
            case "childOrganisationIds" -> "childOrganisationIds";
            default -> order.getProperty();
        };
    }
}
