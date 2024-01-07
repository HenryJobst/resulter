package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.OrganisationType;

public record OrganisationTypeDto(String id) {
    static public OrganisationTypeDto from(OrganisationType organisationType) {
        return new OrganisationTypeDto(organisationType.value());
    }
}
