package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.RaceCupScore;

import java.util.List;

public record RaceCupScoreDto(RaceDto race, List<OrganisationScoreDto> organisationScores) {

    public static RaceCupScoreDto from(RaceCupScore raceCupScore) {
        return new RaceCupScoreDto(RaceDto.from(raceCupScore.race()),
            raceCupScore.organisationScores() != null ?
            raceCupScore.organisationScores().stream().map(OrganisationScoreDto::from).toList() : List.of());
    }
}
