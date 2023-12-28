package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.ClassResult;

public record ClassResultDto(String name) {
    static public ClassResultDto from(ClassResult classResult) {
        return new ClassResultDto(classResult.getClassResultName().value());
    }
}
