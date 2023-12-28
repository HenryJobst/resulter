package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.PersonRaceResult;
import de.jobst.resulter.domain.PersonResult;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.time.Year;

public record PersonResultDto(Long id,
                              Long position,
                              String personName,
                              Year birthYear,
                              Duration runTime,
                              String resultStatus,
                              String organisation)
        implements Comparable<PersonResultDto> {
    static public PersonResultDto from(PersonResult personResult) {
        PersonRaceResult
                personRaceResult =
                personResult.getPersonRaceResults().get().value().stream().findFirst().orElseThrow();
        Double runTime = personRaceResult.getRuntime().value();
        return new PersonResultDto(
                personResult.getId().value(),
                personRaceResult.getPositon().value(),
                personResult.getPerson().get().getPersonName().getFullName(),
                personResult.getPerson().get().getBirthDate().value() != null ?
                        Year.from(personResult.getPerson().get().getBirthDate().value()) : null,
                runTime != null ? Duration.ofSeconds(runTime.longValue()) : null,
                personRaceResult.getState().value(),
                personResult.getOrganisation().get() != null ?
                        personResult.getOrganisation().get().getName().value() : ""
        );
    }

    @Override
    public int compareTo(@NonNull PersonResultDto o) {
        int value = ObjectUtils.compare(position, o.position, true);
        if (value == 0) {
            value = ObjectUtils.compare(runTime, o.runTime, true);
        }
        if (value == 0) {
            value = ObjectUtils.compare(resultStatus, o.resultStatus);
        }
        if (value == 0) {
            value = ObjectUtils.compare(personName, o.personName);
        }
        if (value == 0) {
            value = ObjectUtils.compare(id, o.id);
        }
        return value;
    }
}
