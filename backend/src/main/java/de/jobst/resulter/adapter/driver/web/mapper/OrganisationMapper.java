package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.OrganisationDto;
import de.jobst.resulter.adapter.driver.web.dto.OrganisationKeyDto;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.ObjectUtils;

public class OrganisationMapper {

    private OrganisationMapper() {}

    public static OrganisationDto toDto(
            Organisation organisation, Map<CountryId, Country> countryMap, Map<OrganisationId, Organisation> orgMap) {
        Country country = organisation.getCountry() != null ? countryMap.get(organisation.getCountry()) : null;
        return new OrganisationDto(
                ObjectUtils.isNotEmpty(organisation.getId())
                        ? organisation.getId().value()
                        : 0,
                organisation.getName().value(),
                organisation.getShortName().value(),
                OrganisationTypeMapper.toDto(organisation.getType()),
                country != null ? CountryMapper.toKeyDto(country) : null,
                organisation.getChildOrganisations().stream()
                        .map(orgMap::get)
                        .filter(Objects::nonNull)
                        .map(OrganisationMapper::toKeyDto)
                        .toList());
    }

    public static List<OrganisationDto> toDtos(
            List<Organisation> organisations,
            Map<CountryId, Country> countryMap,
            Map<OrganisationId, Organisation> orgMap) {
        return organisations.stream().map(o -> toDto(o, countryMap, orgMap)).toList();
    }

    public static OrganisationKeyDto toKeyDto(Organisation organisation) {
        return new OrganisationKeyDto(
                ObjectUtils.isNotEmpty(organisation.getId())
                        ? organisation.getId().value()
                        : 0,
                organisation.getName().value());
    }
}
