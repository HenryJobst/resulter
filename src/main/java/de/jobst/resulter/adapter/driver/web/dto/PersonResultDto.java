package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.PersonRaceResult;
import de.jobst.resulter.domain.PersonResult;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.NonNull;

import java.time.Duration;

public record PersonResultDto(Long id, Long position, String personName, Duration runTime, String resultStatus)
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
                runTime != null ? Duration.ofSeconds(runTime.longValue()) : null,
                personRaceResult.getState().value()
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
