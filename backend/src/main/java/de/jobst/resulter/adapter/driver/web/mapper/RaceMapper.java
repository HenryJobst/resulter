package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.RaceDto;
import de.jobst.resulter.domain.Race;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;

public class RaceMapper {

    public static RaceDto toDtoStatic(Race race) {
        assert race.getRaceName() != null;
        return new RaceDto(
                ObjectUtils.isNotEmpty(race.getId()) ? race.getId().value() : 0,
                race.getRaceName().value(),
                race.getRaceNumber().value());
    }

    public static List<RaceDto> toDtos(List<Race> races) {
        return races.stream().map(RaceMapper::toDtoStatic).toList();
    }
}
