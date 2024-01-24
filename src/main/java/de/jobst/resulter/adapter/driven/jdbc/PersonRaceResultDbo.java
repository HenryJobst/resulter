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
@Table(name = "person_race_result")
public class PersonRaceResultDbo {

    @Column("race_number")
    private Long raceNumber;
    @Column("start_time")
    private OffsetDateTime startTime;
    @Column("start_time_zone")
    private String startTimeZone;
    @Column("finish_time")
    private OffsetDateTime finishTime;
    @Column("finish_time_zone")
    private String finishTimeZone;
    @Column("punch_time")
    private Double punchTime;
    @Column("position")
    private Long position;
    @Column("state")
    private ResultStatus state;

    @Column("split_time_list_id")
    @Setter
    private AggregateReference<SplitTimeListDbo, Long> splitTimeList;

    public static PersonRaceResultDbo from(@NonNull PersonRaceResult personRaceResult) {
        PersonRaceResultDbo personRaceResultDbo = new PersonRaceResultDbo();

        if (null != personRaceResult.getStartTime().value()) {
            personRaceResultDbo.setStartTime(personRaceResult.getStartTime().value().toOffsetDateTime());
            personRaceResultDbo.setStartTimeZone(personRaceResult.getStartTime().value().getZone().getId());
        } else {
            personRaceResultDbo.setStartTime(null);
            personRaceResultDbo.setStartTimeZone(null);
        }
        if (null != personRaceResult.getFinishTime().value()) {
            personRaceResultDbo.setFinishTime(personRaceResult.getFinishTime().value().toOffsetDateTime());
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
