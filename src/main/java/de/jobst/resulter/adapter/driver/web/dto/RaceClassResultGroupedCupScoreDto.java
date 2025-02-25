package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.aggregations.RaceClassResultGroupedCupScore;
import java.util.List;

public record RaceClassResultGroupedCupScoreDto(RaceDto race, List<ClassResultScoreDto> classResultScores) {

    public static RaceClassResultGroupedCupScoreDto from(
            RaceClassResultGroupedCupScore raceClassResultGroupedCupScore) {
        return new RaceClassResultGroupedCupScoreDto(
                RaceDto.from(raceClassResultGroupedCupScore.race()),
                raceClassResultGroupedCupScore.classResultScores() != null
                        ? raceClassResultGroupedCupScore.classResultScores().stream()
                                .map(ClassResultScoreDto::from)
                                .toList()
                        : List.of());
    }
}
