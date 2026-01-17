package de.jobst.resulter.adapter.driver.web.dto;

import java.util.Collection;

public record ClassResultDto(String shortName, String name, Long courseId, Collection<PersonResultDto> personResults)
        implements Comparable<ClassResultDto> {

    @Override
    public int compareTo(ClassResultDto o) {
        return name.compareTo(o.name);
    }
}
