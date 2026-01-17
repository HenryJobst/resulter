package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.PersonWithScoreDto;
import de.jobst.resulter.domain.PersonWithScore;

import java.util.List;

public class PersonWithScoreMapper {

    private PersonWithScoreMapper() {
        // Utility class
    }

    public static PersonWithScoreDto toDto(PersonWithScore personWithScore) {
        return new PersonWithScoreDto(
                personWithScore.id().value(),
                personWithScore.score(),
                personWithScore.classResultShortName().value());
    }

    public static List<PersonWithScoreDto> toDtos(List<PersonWithScore> personWithScores) {
        return personWithScores.stream()
                .map(PersonWithScoreMapper::toDto)
                .toList();
    }
}
