package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.ClassResultScores;
import de.jobst.resulter.domain.ClassResultShortName;

import java.util.List;

public record ClassResultScoreDto(ClassResultShortName classResultShortName,
                                  List<PersonWithScoreDto> personWithScores) {
    public static ClassResultScoreDto from(ClassResultScores classResultScores) {
        return new ClassResultScoreDto(classResultScores.classResultShortName(),
            classResultScores.personWithScores().stream().map(PersonWithScoreDto::from).toList());
    }
}
