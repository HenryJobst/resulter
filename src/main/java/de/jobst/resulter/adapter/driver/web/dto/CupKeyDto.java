package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Cup;
import de.jobst.resulter.domain.CupType;
import org.apache.commons.lang3.ObjectUtils;

public record CupKeyDto(Long id, String name, CupType cupType) {

    static public CupKeyDto from(Cup cup) {
        return new CupKeyDto(ObjectUtils.isNotEmpty(cup.getId()) ? cup.getId().value() : 0,
            cup.getName().value(),
            cup.getType());
    }
}
