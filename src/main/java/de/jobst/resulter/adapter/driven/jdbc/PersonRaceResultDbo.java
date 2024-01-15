package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.PersonRaceResult;
import de.jobst.resulter.domain.ResultStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@NoArgsConstructor
@Table(name = "PERSON_RACE_RESULT")
public class PersonRaceResultDbo {

    private PersonResultDbo personResultDbo;

    private Long raceNumber;
    private ZonedDateTime startTime;
    private ZonedDateTime finishTime;
    private Double punchTime;
    private Long position;
    private ResultStatus state;

    public static PersonRaceResultDbo from(@NonNull PersonRaceResult personRaceResult,
                                           @NonNull PersonResultDbo personResultDbo) {
        PersonRaceResultDbo personRaceResultDbo = new PersonRaceResultDbo();

        personRaceResultDbo.setPersonResultDbo(personResultDbo);
        if (ObjectUtils.isNotEmpty(personRaceResult.startTime())) {
            personRaceResultDbo.setStartTime(personRaceResult.startTime().value());
        } else {
            personRaceResultDbo.setStartTime(null);
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.finishTime())) {
            personRaceResultDbo.setFinishTime(personRaceResult.finishTime().value());
        } else {
            personRaceResultDbo.setFinishTime(null);
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.runtime())) {
            personRaceResultDbo.setPunchTime(personRaceResult.runtime().value());
        } else {
            personRaceResultDbo.setPunchTime(null);
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.raceNumber())) {
            personRaceResultDbo.setRaceNumber(personRaceResult.raceNumber().value());
        } else {
            personRaceResultDbo.setRaceNumber(null);
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.position())) {
            personRaceResultDbo.setPosition(personRaceResult.position().value());
        } else {
            personRaceResultDbo.setPosition(null);
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.state())) {
            personRaceResultDbo.setState(personRaceResult.state());
        } else {
            personRaceResultDbo.setState(null);
        }

        return personRaceResultDbo;
    }

    public static Collection<PersonRaceResult> asPersonRaceResults(
        @NonNull List<PersonRaceResultDbo> personRaceResultDbos) {
        return personRaceResultDbos.stream()
            .map(it -> PersonRaceResult.of(it.raceNumber,
                it.getStartTime(),
                it.getFinishTime(),
                it.getPunchTime(),
                it.getPosition(),
                it.getState()))
            .toList();
    }
}
