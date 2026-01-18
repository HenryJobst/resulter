package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.RaceDto;
import de.jobst.resulter.domain.Race;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

@Component
public class RaceMapper {

    @Deprecated(since = "4.6.2", forRemoval = true)
    public static RaceDto toDto(Race race) {
        assert race.getRaceName() != null;
        return new RaceDto(
                ObjectUtils.isNotEmpty(race.getId()) ? race.getId().value() : 0,
                race.getRaceName().value(),
                race.getRaceNumber().value());
    }

    public RaceDto toDtoInstance(Race race) {
        assert race.getRaceName() != null;
        return new RaceDto(
                ObjectUtils.isNotEmpty(race.getId()) ? race.getId().value() : 0,
                race.getRaceName().value(),
                race.getRaceNumber().value());
    }

    public List<RaceDto> toDtos(List<Race> races) {
        return races.stream().map(this::toDtoInstance).toList();
    }
}
