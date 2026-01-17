package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.OrganisationDto;
import de.jobst.resulter.adapter.driver.web.dto.OrganisationKeyDto;
import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class OrganisationMapper {

    private final CountryService countryService;
    private final OrganisationService organisationService;

    public OrganisationMapper(CountryService countryService, OrganisationService organisationService) {
        this.countryService = countryService;
        this.organisationService = organisationService;
    }

    public OrganisationDto toDto(Organisation organisation) {
        Optional<Country> country =
                Optional.ofNullable(organisation.getCountry()).flatMap(countryService::findById);
        return new OrganisationDto(
                ObjectUtils.isNotEmpty(organisation.getId())
                        ? organisation.getId().value()
                        : 0,
                organisation.getName().value(),
                organisation.getShortName().value(),
                OrganisationTypeMapper.toDto(organisation.getType()),
                country.map(CountryMapper::toKeyDto).orElse(null),
                organisation.getChildOrganisations().stream()
                        .map(o -> toKeyDto(organisationService.getById(o)))
                        .toList());
    }

    public OrganisationDto toDto(Organisation organisation, Map<CountryId, Country> countryMap, Map<OrganisationId, Organisation> orgMap) {
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
                        .filter(java.util.Objects::nonNull)
                        .map(OrganisationMapper::toKeyDto)
                        .toList());
    }

    public List<OrganisationDto> toDtos(List<Organisation> organisations) {
        Map<CountryId, Country> countryMap = countryService.batchLoadForOrganisations(organisations);
        Map<OrganisationId, Organisation> orgMap = organisationService.batchLoadChildOrganisations(organisations);
        return organisations.stream()
                .map(o -> toDto(o, countryMap, orgMap))
                .toList();
    }

    public static OrganisationKeyDto toKeyDto(Organisation organisation) {
        return new OrganisationKeyDto(
                ObjectUtils.isNotEmpty(organisation.getId()) ? organisation.getId().value() : 0,
                organisation.getName().value());
    }
}
