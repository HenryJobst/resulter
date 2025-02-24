package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.aggregations.OrganisationScore;

import java.util.List;

public record OrganisationScoreDto(OrganisationDto organisation, Double score,
                                   List<PersonWithScoreDto> personWithScores) {
    public static OrganisationScoreDto from(OrganisationScore organisationScore) {
        return new OrganisationScoreDto(OrganisationDto.from(organisationScore.organisation()),
            organisationScore.score(),
            organisationScore.personWithScores().stream().map(PersonWithScoreDto::from).toList());
    }
}
