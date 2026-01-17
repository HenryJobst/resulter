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

import java.util.List;
import java.util.Map;

@Component
public class OrganisationScoreMapper {

    private final OrganisationMapper organisationMapper;
    private final CountryService countryService;
    private final OrganisationService organisationService;

    public OrganisationScoreMapper(
            OrganisationMapper organisationMapper,
            CountryService countryService,
            OrganisationService organisationService) {
        this.organisationMapper = organisationMapper;
        this.countryService = countryService;
        this.organisationService = organisationService;
    }

    public OrganisationScoreDto toDto(OrganisationScore organisationScore) {
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

    public List<OrganisationScoreDto> toDtos(List<OrganisationScore> organisationScores) {
        List<Organisation> organisations = organisationScores.stream()
                .map(OrganisationScore::organisation)
                .toList();
        Map<CountryId, Country> countryMap = countryService.batchLoadForOrganisations(organisations);
        Map<OrganisationId, Organisation> orgMap = organisationService.batchLoadChildOrganisations(organisations);
        return organisationScores.stream()
                .map(o -> toDto(o, countryMap, orgMap))
                .toList();
    }
}
