package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.CountryService;
import de.jobst.resulter.domain.aggregations.RaceOrganisationGroupedCupScore;

import java.util.List;

public record RaceOrganisationGroupedCupScoreDto(RaceDto race, List<OrganisationScoreDto> organisationScores) {

    public static RaceOrganisationGroupedCupScoreDto from(RaceOrganisationGroupedCupScore raceCupScore, CountryService countryService) {
        return new RaceOrganisationGroupedCupScoreDto(
                RaceDto.from(raceCupScore.race()),
                raceCupScore.organisationScores() != null
                        ? raceCupScore.organisationScores().stream()
                                .map(o -> OrganisationScoreDto.from(o, countryService))
                                .toList()
                        : List.of());
    }
}
