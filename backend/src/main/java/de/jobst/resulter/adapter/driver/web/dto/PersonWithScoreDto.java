package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.PersonWithScore;

public record PersonWithScoreDto(Long personId, double score, String classShortName) {

    public static PersonWithScoreDto from(PersonWithScore personWithScore) {
        return new PersonWithScoreDto(personWithScore.id().value(), personWithScore.score(),
            personWithScore.classResultShortName().value());
    }
}
