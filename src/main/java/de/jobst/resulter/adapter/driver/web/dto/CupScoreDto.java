package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.CupScore;

public record CupScoreDto(Double score) {

    static public CupScoreDto from(CupScore cupScore) {
        return new CupScoreDto(cupScore.value());
    }
}
