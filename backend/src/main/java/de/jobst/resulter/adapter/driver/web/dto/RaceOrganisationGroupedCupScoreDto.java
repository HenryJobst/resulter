package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.adapter.driver.web.mapper.OrganisationScoreMapper;
import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.domain.aggregations.RaceOrganisationGroupedCupScore;

import java.util.List;

public record RaceOrganisationGroupedCupScoreDto(RaceDto race, List<OrganisationScoreDto> organisationScores) {

    public static RaceOrganisationGroupedCupScoreDto from(
            RaceOrganisationGroupedCupScore raceCupScore,
            CountryService countryService,
            OrganisationService organisationService,
            OrganisationScoreMapper organisationScoreMapper) {
        return new RaceOrganisationGroupedCupScoreDto(
                RaceDto.from(raceCupScore.race()),
                raceCupScore.organisationScores() != null
                        ? raceCupScore.organisationScores().stream()
                                .map(o -> organisationScoreMapper.toDto(o, countryService, organisationService))
                                .toList()
                        : List.of());
    }
}
