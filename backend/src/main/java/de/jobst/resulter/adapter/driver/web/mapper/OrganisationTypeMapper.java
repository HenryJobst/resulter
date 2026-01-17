package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.OrganisationTypeDto;
import de.jobst.resulter.domain.OrganisationType;

public class OrganisationTypeMapper {

    private OrganisationTypeMapper() {
        // Utility class
    }

    public static OrganisationTypeDto toDto(OrganisationType type) {
        return new OrganisationTypeDto(type.value());
    }
}
