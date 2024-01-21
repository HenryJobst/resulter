package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.PersonRaceResult;
import de.jobst.resulter.domain.PersonResult;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.NonNull;

import java.time.Duration;

public record PersonResultDto(Long position, Long personId, Duration runTime, String resultStatus, Long organisationId)
    implements Comparable<PersonResultDto> {

    static public PersonResultDto from(PersonResult personResult) {
        PersonRaceResult personRaceResult = personResult.personRaceResults().value().stream().findFirst().orElseThrow();
        Double runTime = personRaceResult.getRuntime().value();
        return new PersonResultDto(personRaceResult.getPosition().value(),
            personResult.personId() != null ? personResult.personId().value() : null,
            runTime != null ? Duration.ofSeconds(runTime.longValue()) : null,
            personRaceResult.getState().value(),
            personResult.organisationId() != null ? personResult.organisationId().value() : null);
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
            value = ObjectUtils.compare(personId, o.personId);
        }
        return value;
    }
}
