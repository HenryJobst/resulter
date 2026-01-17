package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.adapter.driver.web.mapper.OrganisationScoreMapper;
import de.jobst.resulter.adapter.driver.web.mapper.RaceMapper;
import de.jobst.resulter.domain.aggregations.RaceOrganisationGroupedCupScore;

import java.util.List;

public record RaceOrganisationGroupedCupScoreDto(RaceDto race, List<OrganisationScoreDto> organisationScores) {

    public static RaceOrganisationGroupedCupScoreDto from(
            RaceOrganisationGroupedCupScore raceCupScore,
            OrganisationScoreMapper organisationScoreMapper) {
        return new RaceOrganisationGroupedCupScoreDto(
                RaceMapper.toDto(raceCupScore.race()),
                raceCupScore.organisationScores() != null
                        ? organisationScoreMapper.toDtos(raceCupScore.organisationScores())
                        : List.of());
    }
}
