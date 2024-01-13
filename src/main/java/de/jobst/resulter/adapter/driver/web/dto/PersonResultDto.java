package de.jobst.resulter.adapter.driver.web.dto;

import de.jobst.resulter.domain.PersonRaceResult;
import de.jobst.resulter.domain.PersonResult;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public record PersonResultDto(Long id, Long position, Long personId, Duration runTime, String resultStatus,
                              Long organisationId, List<CupScoreDto> cupScores) implements Comparable<PersonResultDto> {

    static public PersonResultDto from(PersonResult personResult) {
        PersonRaceResult personRaceResult =
            personResult.getPersonRaceResults().get().value().stream().findFirst().orElseThrow();
        Double runTime = personRaceResult.getRuntime().value();
        return new PersonResultDto(personResult.getId().value(),
            personRaceResult.getPosition().value(),
            personResult.getPersonId() != null ? personResult.getPersonId().value() : null,
            runTime != null ? Duration.ofSeconds(runTime.longValue()) : null,
            personRaceResult.getState().value(),
            personResult.getOrganisationId() != null ? personResult.getOrganisationId().value() : null,
            personRaceResult.getCupScores().get() != null ?
            personRaceResult.getCupScores().get().values().stream().map(CupScoreDto::from).toList() :
            new ArrayList<>());
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
        if (value == 0) {
            value = ObjectUtils.compare(id, o.id);
        }
        return value;
    }
}
