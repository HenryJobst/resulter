package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.PersonRaceResult;
import de.jobst.resulter.domain.ResultStatus;
import lombok.*;
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
    @Setter
    private AggregateReference<SplitTimeListDbo, Long> splitTimeList;

    public static PersonRaceResultDbo from(@NonNull PersonRaceResult personRaceResult) {
        PersonRaceResultDbo personRaceResultDbo = new PersonRaceResultDbo();

        if (personRaceResult.getStartTime().value() != null) {
            personRaceResultDbo.setStartTime(personRaceResult.getStartTime().value().toOffsetDateTime());
            personRaceResultDbo.setStartTimeZone(personRaceResult.getStartTime().value().getZone().getId());
        } else {
            personRaceResultDbo.setStartTime(null);
            personRaceResultDbo.setStartTimeZone(null);
        }
        if (personRaceResult.getFinishTime().value() != null) {
            personRaceResultDbo.setFinishTime(personRaceResult.getFinishTime().value().toOffsetDateTime());
            personRaceResultDbo.setFinishTimeZone(personRaceResult.getFinishTime().value().getZone().getId());
        } else {
            personRaceResultDbo.setFinishTime(null);
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.getRuntime())) {
            personRaceResultDbo.setPunchTime(personRaceResult.getRuntime().value());
        } else {
            personRaceResultDbo.setPunchTime(null);
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.getRaceNumber())) {
            personRaceResultDbo.setRaceNumber(personRaceResult.getRaceNumber().value());
        } else {
            personRaceResultDbo.setRaceNumber(null);
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.getPosition())) {
            personRaceResultDbo.setPosition(personRaceResult.getPosition().value());
        } else {
            personRaceResultDbo.setPosition(null);
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.getState())) {
            personRaceResultDbo.setState(personRaceResult.getState());
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
