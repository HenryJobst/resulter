package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Sort;

import java.util.List;

public record OrganisationDto(Long id, String name, String shortName, OrganisationTypeDto type, Long countryId,
                              List<Long> organisationIds) {

    static public OrganisationDto from(Organisation organisation) {
        return new OrganisationDto(ObjectUtils.isNotEmpty(organisation.getId()) ? organisation.getId().value() : 0,
            organisation.getName().value(),
            organisation.getShortName().value(),
            OrganisationTypeDto.from(organisation.getType()),
            organisation.getCountryId() != null ? organisation.getCountryId().value() : null,
            organisation.getChildOrganisationIds().stream().map(OrganisationId::value).toList());
    }

    public static String mapOrdersDtoToDomain(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id" -> "id.value";
            case "name" -> "name.value";
            case "shortName" -> "shortName.value";
            case "type" -> "type.id";
            case "countryId" -> "organisation.countryId.value";
            case "organisationIds" -> "childOrganisationIds";
            default -> order.getProperty();
        };
    }

    public static String mapOrdersDomainToDto(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id.value" -> "id";
            case "name.value" -> "name";
            case "shortName.value" -> "shortName";
            case "type.id" -> "type";
            case "organisation.countryId.value" -> "countryId";
            case "childOrganisationIds" -> "organisationIds";
            default -> order.getProperty();
        };
    }
}
