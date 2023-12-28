package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.PersonResult;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.NonNull;

import java.time.Duration;

public record PersonResultDto(Long id, String personName, Duration runTime, String resultStatus)
        implements Comparable<PersonResultDto> {
    static public PersonResultDto from(PersonResult personResult) {
        Double value = personResult.getPersonRaceResults()
                .get()
                .value()
                .stream()
                .findFirst()
                .orElseThrow()
                .getRuntime()
                .value();
        return new PersonResultDto(
                personResult.getId().value(), personResult.getPerson().get().getPersonName().getFullName(),
                value != null ? Duration.ofSeconds(value.longValue()) : null,
                personResult.getPersonRaceResults().get().value().stream().findFirst().orElseThrow().getState().value()
        );
    }

    @Override
    public int compareTo(@NonNull PersonResultDto o) {
        int value = ObjectUtils.compare(runTime, o.runTime);
        if (value == 0) {
            value = ObjectUtils.compare(resultStatus, o.resultStatus);
        }
        if (value == 0) {
            value = ObjectUtils.compare(id, o.id);
        }
        return value;
    }
}
