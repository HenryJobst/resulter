package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.ClassResultScoreDto;
import de.jobst.resulter.domain.ClassResultScores;

import java.util.List;

public class ClassResultScoreMapper {

    private ClassResultScoreMapper() {
        // Utility class
    }

    public static ClassResultScoreDto toDto(ClassResultScores classResultScores) {
        return new ClassResultScoreDto(
                classResultScores.classResultShortName().value(),
                PersonWithScoreMapper.toDtos(classResultScores.personWithScores()));
    }

    public static List<ClassResultScoreDto> toDtos(List<ClassResultScores> classResultScores) {
        return classResultScores.stream()
                .map(ClassResultScoreMapper::toDto)
                .toList();
    }
}
