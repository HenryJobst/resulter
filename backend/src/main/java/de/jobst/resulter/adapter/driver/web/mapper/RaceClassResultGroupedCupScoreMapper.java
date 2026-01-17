package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.RaceClassResultGroupedCupScoreDto;
import de.jobst.resulter.domain.aggregations.RaceClassResultGroupedCupScore;
import java.util.List;

public class RaceClassResultGroupedCupScoreMapper {

    private RaceClassResultGroupedCupScoreMapper() {
        // Utility class
    }

    public static RaceClassResultGroupedCupScoreDto toDto(
            RaceClassResultGroupedCupScore raceClassResultGroupedCupScore) {
        return new RaceClassResultGroupedCupScoreDto(
                RaceMapper.toDto(raceClassResultGroupedCupScore.race()),
                raceClassResultGroupedCupScore.classResultScores() != null
                        ? ClassResultScoreMapper.toDtos(raceClassResultGroupedCupScore.classResultScores())
                        : List.of());
    }

    public static List<RaceClassResultGroupedCupScoreDto> toDtos(
            List<RaceClassResultGroupedCupScore> raceClassResultGroupedCupScores) {
        return raceClassResultGroupedCupScores.stream()
                .map(RaceClassResultGroupedCupScoreMapper::toDto)
                .toList();
    }
}
