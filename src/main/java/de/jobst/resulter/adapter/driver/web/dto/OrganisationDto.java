package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Organisation;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Sort;

import java.util.List;

public record OrganisationDto(Long id, String name, String shortName, OrganisationTypeDto type, CountryKeyDto country,
                              List<OrganisationKeyDto> childOrganisations) {

    static public OrganisationDto from(Organisation organisation) {
        return new OrganisationDto(ObjectUtils.isNotEmpty(organisation.getId()) ? organisation.getId().value() : 0,
            organisation.getName().value(),
            organisation.getShortName().value(),
            OrganisationTypeDto.from(organisation.getType()),
            organisation.getCountry() != null ? CountryKeyDto.from(organisation.getCountry()) : null,
            organisation.getChildOrganisations().stream().map(OrganisationKeyDto::from).toList());
    }

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
