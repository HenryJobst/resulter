package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.PersonId;
import de.jobst.resulter.domain.PersonWithScore;

public record PersonWithScoreDto(PersonId personId, double score, String classShortName) {

    public static PersonWithScoreDto from(PersonWithScore personWithScore) {
        return new PersonWithScoreDto(personWithScore.id(), personWithScore.score(),
            personWithScore.classResultShortName().value());
    }
}
