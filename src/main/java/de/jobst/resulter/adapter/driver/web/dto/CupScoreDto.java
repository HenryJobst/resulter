package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.CupScore;

public record CupScoreDto(CupTypeDto type, Double score) {
    static public CupScoreDto from(CupScore cupScore) {
        return new CupScoreDto(CupTypeDto.from(cupScore.id().type()), cupScore.value());
    }
}
