package de.jobst.resulter.domain;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

@ValueObject
@Getter
@AllArgsConstructor
public class PersonRaceResult implements Comparable<PersonRaceResult> {

    private ClassResultShortName classResultShortName;

    @Setter
    private PersonId personId;

    private DateTime startTime;

    private DateTime finishTime;

    private PunchTime runtime;

    private Position position;

    private ResultStatus state;

    private RaceNumber raceNumber;

    @Nullable
    @Setter
    private SplitTimeListId splitTimeListId;

    public static PersonRaceResult of(
            String classResultShortName,
            Long personId,
            @Nullable ZonedDateTime startTime,
            @Nullable ZonedDateTime finishTime,
            @Nullable Double punchTime,
            @Nullable Long position,
            Byte raceNumber,
            ResultStatus resultState) {
        return PersonRaceResult.of(
                classResultShortName,
                personId,
                startTime,
                finishTime,
                punchTime,
                position,
                resultState,
                raceNumber,
                null);
    }

    public static PersonRaceResult of(
            String classResultShortName,
            Long personId,
            @Nullable ZonedDateTime startTime,
            @Nullable ZonedDateTime finishTime,
            @Nullable Double punchTime,
            @Nullable Long position,
            ResultStatus resultState,
            Byte raceNumber,
            @Nullable SplitTimeListId splitTimeListId) {
        return new PersonRaceResult(
                ClassResultShortName.of(classResultShortName),
                PersonId.of(personId),
                DateTime.of(startTime),
                DateTime.of(finishTime),
                PunchTime.of(punchTime),
                Position.of(position),
                resultState,
                RaceNumber.of(raceNumber),
                splitTimeListId);
    }

    @Override
    public int compareTo(PersonRaceResult o) {
        int val = this.raceNumber.compareTo(o.raceNumber);
        if (val == 0) {
            if (this.classResultShortName.equals(o.classResultShortName)) {
                // use position, when race number and class are equal
                val = this.position.compareTo(o.position);
            } else {
                // use runtime, when class is different (position doesn't matter)
                val = this.runtime.compareTo(o.runtime);
            }
        }
        if (val == 0) {
            val = this.runtime.compareTo(o.runtime);
        }
        if (val == 0) {
            val = this.personId.compareTo(o.personId);
        }
        if (val == 0) {
            val = this.classResultShortName.compareTo(o.classResultShortName);
        }
        return val;
    }
}
