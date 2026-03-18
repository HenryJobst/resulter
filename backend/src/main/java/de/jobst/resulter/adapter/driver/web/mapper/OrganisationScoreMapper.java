package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.OrganisationScoreDto;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.aggregations.OrganisationScore;
import java.util.List;
import java.util.Map;

public class OrganisationScoreMapper {

    public static OrganisationScoreDto toDto(
            OrganisationScore organisationScore,
            Map<CountryId, Country> countryMap,
            Map<OrganisationId, Organisation> orgMap) {
        return new OrganisationScoreDto(
                OrganisationMapper.toDto(organisationScore.organisation(), countryMap, orgMap),
                organisationScore.score(),
                organisationScore.personWithScores().stream()
                        .map(PersonWithScoreMapper::toDto)
                        .toList());
    }

    public static List<OrganisationScoreDto> toDtos(
            List<OrganisationScore> organisationScores,
            Map<CountryId, Country> countryMap,
            Map<OrganisationId, Organisation> orgMap) {
        return organisationScores.stream()
                .map(o -> toDto(o, countryMap, orgMap))
                .toList();
    }
}
