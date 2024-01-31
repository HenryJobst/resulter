package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.ClassResult;
import org.springframework.lang.NonNull;

import java.util.Collection;

public record ClassResultDto(String name, Collection<PersonResultDto> personResults)
    implements Comparable<ClassResultDto> {

    static public ClassResultDto from(ClassResult classResult) {
        return new ClassResultDto(classResult.classResultName().value(),
            classResult.personResults().value().stream().map(PersonResultDto::from).sorted().toList());
    }

    @Override
    public int compareTo(@NonNull ClassResultDto o) {
        return name.compareTo(o.name);
    }
}
