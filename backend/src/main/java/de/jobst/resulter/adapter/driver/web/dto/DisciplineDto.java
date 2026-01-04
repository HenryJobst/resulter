package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Discipline;

public record DisciplineDto(String id) {

    static public DisciplineDto from(Discipline discipline) {
        return new DisciplineDto(discipline.value());
    }
}
