package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Cup;
import org.apache.commons.lang3.ObjectUtils;

public record CupDto(Long id,
                     String name,
                     String type) {
    static public CupDto from(Cup cup) {
        return new CupDto(
                ObjectUtils.isNotEmpty(cup.getId()) ?
                        cup.getId().value() : 0,
                cup.getName().value(),
                cup.getType().value()
        );
    }
}
