package de.jobst.resulter.adapter.driver.web.dto;

import java.time.Duration;
import org.apache.commons.lang3.ObjectUtils;

public record PersonResultDto(
        Long position, Long personId, Duration runTime, String resultStatus, Long organisationId, Byte raceNumber)
        implements Comparable<PersonResultDto> {

    @Override
    public int compareTo(PersonResultDto o) {
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
        if (value == 0) {
            value = ObjectUtils.compare(raceNumber, o.raceNumber);
        }
        return value;
    }
}
