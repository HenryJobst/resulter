package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.ClassResult;

import java.util.Collection;

public record ClassResultDto(long id, String name, Collection<PersonResultDto> personResults) {
    static public ClassResultDto from(ClassResult classResult) {
        return new ClassResultDto(
                classResult.getId().value(),
                classResult.getClassResultName().value(),
                classResult.getPersonResults().get().value().stream().map(PersonResultDto::from).sorted().toList());
    }
}
