package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Organisation;
import org.apache.commons.lang3.ObjectUtils;

public record OrganisationDto(Long id,
                              String name,
                              String shortName,
                              String type) {
    static public OrganisationDto from(Organisation organisation) {
        return new OrganisationDto(
                ObjectUtils.isNotEmpty(organisation.getId()) ?
                        organisation.getId().value() : 0,
                organisation.getName().value(),
                organisation.getShortName().value(),
                organisation.getType().value()
        );
    }
}
