package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.PersonRaceResult;
import de.jobst.resulter.domain.ResultStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__(@PersistenceCreator))
@NoArgsConstructor
@Table(name = "PERSON_RACE_RESULT")
public class PersonRaceResultDbo {

    private Long raceNumber;
    private OffsetDateTime startTime;
    private String startTimeZone;
    private OffsetDateTime finishTime;
    private String finishTimeZone;
    private Double punchTime;
    private Long position;
    private ResultStatus state;

    @Column("SPLIT_TIME_LIST_ID")
    private AggregateReference<SplitTimeListDbo, Long> splitTimeList;

    public static PersonRaceResultDbo from(@NonNull PersonRaceResult personRaceResult) {
        PersonRaceResultDbo personRaceResultDbo = new PersonRaceResultDbo();

        if (personRaceResult.startTime().value() != null) {
            personRaceResultDbo.setStartTime(personRaceResult.startTime().value().toOffsetDateTime());
            personRaceResultDbo.setStartTimeZone(personRaceResult.startTime().value().getZone().getId());
        } else {
            personRaceResultDbo.setStartTime(null);
            personRaceResultDbo.setStartTimeZone(null);
        }
        if (personRaceResult.finishTime().value() != null) {
            personRaceResultDbo.setFinishTime(personRaceResult.finishTime().value().toOffsetDateTime());
            personRaceResultDbo.setFinishTimeZone(personRaceResult.finishTime().value().getZone().getId());
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
                it.startTime != null ? it.startTime.atZoneSameInstant(ZoneId.of(it.startTimeZone)) : null,
                it.finishTime != null ? it.finishTime.atZoneSameInstant(ZoneId.of(it.finishTimeZone)) : null,
                it.getPunchTime(),
                it.getPosition(),
                it.getState()))
            .toList();
    }
}
