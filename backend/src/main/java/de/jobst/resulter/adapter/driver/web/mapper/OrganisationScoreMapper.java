package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.OrganisationScoreDto;
import de.jobst.resulter.adapter.driver.web.dto.PersonWithScoreDto;
import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.aggregations.OrganisationScore;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrganisationScoreMapper {

    private final OrganisationMapper organisationMapper;

    public OrganisationScoreMapper(OrganisationMapper organisationMapper) {
        this.organisationMapper = organisationMapper;
    }

    public OrganisationScoreDto toDto(
            OrganisationScore organisationScore,
            CountryService countryService,
            OrganisationService organisationService) {
        return new OrganisationScoreDto(
                organisationMapper.toDto(organisationScore.organisation()),
                organisationScore.score(),
                organisationScore.personWithScores().stream()
                        .map(PersonWithScoreDto::from)
                        .toList());
    }

    public OrganisationScoreDto toDto(
            OrganisationScore organisationScore,
            Map<CountryId, Country> countryMap,
            Map<OrganisationId, Organisation> orgMap) {
        return new OrganisationScoreDto(
                organisationMapper.toDto(organisationScore.organisation(), countryMap, orgMap),
                organisationScore.score(),
                organisationScore.personWithScores().stream()
                        .map(PersonWithScoreDto::from)
                        .toList());
    }
}
