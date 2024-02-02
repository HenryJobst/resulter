package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.ClassResult;
import org.springframework.lang.NonNull;

import java.util.Collection;

public record ClassResultDto(String shortName, String name, Long courseId, Collection<PersonResultDto> personResults)
    implements Comparable<ClassResultDto> {

    static public ClassResultDto from(ClassResult classResult) {
        return new ClassResultDto(classResult.classResultShortName().value(),
            classResult.classResultName().value(),
            classResult.courseId() != null ? classResult.courseId().value() : null,
            classResult.personResults().value().stream().map(PersonResultDto::from).sorted().toList());
    }

    @Override
    public int compareTo(@NonNull ClassResultDto o) {
        return name.compareTo(o.name);
    }
}
