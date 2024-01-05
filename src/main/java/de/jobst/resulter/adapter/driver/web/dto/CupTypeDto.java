package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.CupType;

public record CupTypeDto(String id) {
    static public CupTypeDto from(CupType cupType) {
        return new CupTypeDto(cupType.value());
    }
}
