package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Organisation;
import org.apache.commons.lang3.ObjectUtils;

public record OrganisationKeyDto(Long id, String name) {

    static public OrganisationKeyDto from(Organisation organisation) {
        return new OrganisationKeyDto(ObjectUtils.isNotEmpty(organisation.getId()) ? organisation.getId().value() : 0,
            organisation.getName().value());
    }
}
