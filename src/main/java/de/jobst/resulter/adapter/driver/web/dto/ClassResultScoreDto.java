package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.ClassResultScores;

import java.util.List;

public record ClassResultScoreDto(String classResultShortName,
                                  List<PersonWithScoreDto> personWithScores) {
    public static ClassResultScoreDto from(ClassResultScores classResultScores) {
        return new ClassResultScoreDto(classResultScores.classResultShortName().value(),
            classResultScores.personWithScores().stream().map(PersonWithScoreDto::from).toList());
    }
}
