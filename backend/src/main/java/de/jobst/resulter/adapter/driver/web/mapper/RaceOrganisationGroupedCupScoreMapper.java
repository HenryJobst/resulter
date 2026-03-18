package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.RaceOrganisationGroupedCupScoreDto;
import de.jobst.resulter.domain.Country;
import de.jobst.resulter.domain.CountryId;
import de.jobst.resulter.domain.Organisation;
import de.jobst.resulter.domain.OrganisationId;
import de.jobst.resulter.domain.aggregations.RaceOrganisationGroupedCupScore;
import java.util.List;
import java.util.Map;

public class RaceOrganisationGroupedCupScoreMapper {

    public static RaceOrganisationGroupedCupScoreDto toDto(
            RaceOrganisationGroupedCupScore raceCupScore,
            Map<CountryId, Country> countryMap,
            Map<OrganisationId, Organisation> orgMap) {
        return new RaceOrganisationGroupedCupScoreDto(
                RaceMapper.toDtoStatic(raceCupScore.race()),
                raceCupScore.organisationScores() != null
                        ? OrganisationScoreMapper.toDtos(raceCupScore.organisationScores(), countryMap, orgMap)
                        : List.of());
    }
}
