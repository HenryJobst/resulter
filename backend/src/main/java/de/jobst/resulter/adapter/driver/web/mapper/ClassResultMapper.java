package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.ClassResultDto;
import de.jobst.resulter.domain.ClassResult;

public class ClassResultMapper {

    private ClassResultMapper() {}

    public static ClassResultDto toDto(ClassResult classResult) {
        return new ClassResultDto(
                classResult.classResultShortName().value(),
                classResult.classResultName().value(),
                classResult.courseId() != null ? classResult.courseId().value() : null,
                classResult.personResults().value().stream()
                        .map(PersonResultMapper::toDto)
                        .sorted()
                        .toList());
    }
}
