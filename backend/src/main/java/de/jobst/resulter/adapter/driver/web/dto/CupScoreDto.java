package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.CupScore;
import org.apache.commons.lang3.ObjectUtils;

public record CupScoreDto(Long personId, String classShortName, Double score) implements Comparable<CupScoreDto> {

    static public CupScoreDto from(CupScore cupScore) {
        return new CupScoreDto(cupScore.personId().value(), cupScore.classResultShortName().value(), cupScore.score());
    }

    @Override
    public int compareTo(CupScoreDto o) {
        int value = ObjectUtils.compare(classShortName, o.classShortName, true);
        if (value == 0) {
            value = Double.compare(score, o.score);
        }
        if (value == 0) {
            value = ObjectUtils.compare(personId, o.personId, true);
        }
        return value;
    }
}
