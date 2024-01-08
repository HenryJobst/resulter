package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Organisation;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

public record OrganisationDto(Long id,
                              String name,
                              String shortName,
                              OrganisationTypeDto type,
                              CountryDto country,
                              List<OrganisationDto> organisations) {
    static public OrganisationDto from(Organisation organisation) {
        return new OrganisationDto(
                ObjectUtils.isNotEmpty(organisation.getId()) ?
                        organisation.getId().value() : 0,
                organisation.getName().value(),
                organisation.getShortName().value(),
                OrganisationTypeDto.from(organisation.getType()),
                organisation.getCountry().isLoaded() ?
                        CountryDto.from(organisation.getCountry().get()) : null,
                organisation.getParentOrganisations().isLoaded() ?
                        organisation.getParentOrganisations()
                                .get()
                                .value()
                                .stream()
                                .sorted()
                                .map(OrganisationDto::from)
                                .toList() :
                        new ArrayList<>()
        );
    }
}
