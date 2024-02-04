package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Gender;

public record GenderDto(String id) {

    static public GenderDto from(Gender gender) {
        return new GenderDto(gender.name());
    }
}
