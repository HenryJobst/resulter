package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.Race;
import org.apache.commons.lang3.ObjectUtils;

public record RaceDto(Long id, String name, Byte number) {

    static public RaceDto from(Race race) {
        assert race.getRaceName() != null;
        return new RaceDto(ObjectUtils.isNotEmpty(race.getId()) ? race.getId().value() : 0,
            race.getRaceName().value(),
            race.getRaceNumber().value());
    }
}
