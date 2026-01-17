package de.jobst.resulter.adapter.driver.web.mapper;

import de.jobst.resulter.adapter.driver.web.dto.PersonResultDto;
import de.jobst.resulter.domain.PersonRaceResult;
import de.jobst.resulter.domain.PersonResult;
import java.time.Duration;
import java.util.List;

public class PersonResultMapper {

    private PersonResultMapper() {}

    public static PersonResultDto toDto(PersonResult personResult) {
        PersonRaceResult personRaceResult =
                personResult.personRaceResults().value().stream().findFirst().orElseThrow();
        Double runTime = personRaceResult.getRuntime().value();
        return new PersonResultDto(
                personRaceResult.getPosition().value(),
                personResult.personId().value(),
                runTime != null ? Duration.ofSeconds(runTime.longValue()) : null,
                personRaceResult.getState().value(),
                personResult.organisationId() != null
                        ? personResult.organisationId().value()
                        : null,
                personRaceResult.getRaceNumber().value());
    }

    public static List<PersonResultDto> toDtos(List<PersonResult> personResults) {
        return personResults.stream().map(PersonResultMapper::toDto).sorted().toList();
    }
}
