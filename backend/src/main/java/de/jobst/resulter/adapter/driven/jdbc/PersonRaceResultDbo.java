package de.jobst.resulter.adapter.driven.jdbc;

import de.jobst.resulter.domain.PersonRaceResult;
import de.jobst.resulter.domain.ResultStatus;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE, onConstructor_ =@PersistenceCreator)
@NoArgsConstructor
@Table(name = "person_race_result")
public class PersonRaceResultDbo {

    @NonNull
    @Column("class_result_short_name")
    private String classResultShortName;
    @NonNull
    @Column("person_id")
    private AggregateReference<PersonDbo, Long> person;
    @Nullable
    @Column("start_time")
    private Timestamp startTime;
    @Nullable
    @Column("start_time_zone")
    private String startTimeZone;
    @Column("finish_time")
    private Timestamp finishTime;
    @Column("finish_time_zone")
    private String finishTimeZone;
    @Column("punch_time")
    private Double punchTime;
    @Column("position")
    private Long position;
    @Column("race_number")
    private Byte raceNumber;
    @Column("state")
    private ResultStatus state;

    @Nullable
    @Column("split_time_list_id")
    @Setter
    private AggregateReference<SplitTimeListDbo, Long> splitTimeList;

    public static PersonRaceResultDbo from(@NonNull PersonRaceResult personRaceResult) {
        PersonRaceResultDbo personRaceResultDbo = new PersonRaceResultDbo();
        personRaceResultDbo.setClassResultShortName(Objects.requireNonNull(Objects.requireNonNull(personRaceResult.getClassResultShortName())
            .value()));
        personRaceResultDbo.setPerson(AggregateReference.to(personRaceResult.getPersonId().value()));

        if (null != personRaceResult.getStartTime().value()) {
            personRaceResultDbo.setStartTime(Timestamp.from(personRaceResult.getStartTime().value().toInstant()));
            personRaceResultDbo.setStartTimeZone(personRaceResult.getStartTime().value().getZone().getId());
        } else {
            personRaceResultDbo.setStartTime(null);
            personRaceResultDbo.setStartTimeZone(null);
        }
        if (null != personRaceResult.getFinishTime().value()) {
            personRaceResultDbo.setFinishTime(Timestamp.from(personRaceResult.getFinishTime().value().toInstant()));
            personRaceResultDbo.setFinishTimeZone(personRaceResult.getFinishTime().value().getZone().getId());
        } else {
            personRaceResultDbo.setFinishTime(null);
            personRaceResultDbo.setFinishTimeZone(null);
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.getRuntime())) {
            personRaceResultDbo.setPunchTime(personRaceResult.getRuntime().value());
        } else {
            personRaceResultDbo.setPunchTime(null);
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.getPosition())) {
            personRaceResultDbo.setPosition(personRaceResult.getPosition().value());
        } else {
            personRaceResultDbo.setPosition(null);
        }
        if (ObjectUtils.isNotEmpty(personRaceResult.getRaceNumber())) {
            personRaceResultDbo.setRaceNumber(personRaceResult.getRaceNumber().value());
        } else {
            personRaceResultDbo.setRaceNumber(null);
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
            .map(it -> PersonRaceResult.of(it.classResultShortName,
                it.person.getId(),
                it.startTime != null ?
                it.startTime.toInstant().atZone(ZoneId.of(Objects.requireNonNull(it.startTimeZone))) :
                null,
                it.finishTime != null ? it.finishTime.toInstant().atZone(ZoneId.of(it.finishTimeZone)) :
                null,
                it.getPunchTime(),
                it.getPosition(),
                it.getRaceNumber(),
                it.getState()))
            .toList();
    }
}
