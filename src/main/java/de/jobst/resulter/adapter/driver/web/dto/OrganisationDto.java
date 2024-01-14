package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import org.apache.commons.lang3.ObjectUtils;

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
}
