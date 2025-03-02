package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.application.port.OrganisationService;
import de.jobst.resulter.application.port.CountryService;
import de.jobst.resulter.domain.aggregations.OrganisationScore;

import java.util.List;

public record OrganisationScoreDto(
        OrganisationDto organisation, Double score, List<PersonWithScoreDto> personWithScores) {
    public static OrganisationScoreDto from(
            OrganisationScore organisationScore,
            CountryService countryService,
            OrganisationService organisationService) {
        return new OrganisationScoreDto(
                OrganisationDto.from(organisationScore.organisation(), countryService, organisationService),
                organisationScore.score(),
                organisationScore.personWithScores().stream()
                        .map(PersonWithScoreDto::from)
                        .toList());
    }
}
