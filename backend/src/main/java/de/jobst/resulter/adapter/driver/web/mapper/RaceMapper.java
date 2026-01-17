package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.RaceDto;
import de.jobst.resulter.domain.Race;
import org.apache.commons.lang3.ObjectUtils;

public class RaceMapper {

    private RaceMapper() {
        // Utility class
    }

    public static RaceDto toDto(Race race) {
        assert race.getRaceName() != null;
        return new RaceDto(
                ObjectUtils.isNotEmpty(race.getId()) ? race.getId().value() : 0,
                race.getRaceName().value(),
                race.getRaceNumber().value());
    }
}
