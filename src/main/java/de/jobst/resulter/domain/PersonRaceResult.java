package de.jobst.resulter.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor
public class PersonRaceResult implements Comparable<PersonRaceResult> {

    @NonNull
    private ClassResultShortName classResultShortName;
    @NonNull
    @Setter
    private PersonId personId;
    @NonNull
    private DateTime startTime;
    @NonNull
    private DateTime finishTime;
    @NonNull
    private PunchTime runtime;
    @NonNull
    private Position position;
    @NonNull
    private ResultStatus state;
    @NonNull
    private RaceNumber raceNumber;
    @Nullable
    @Setter
    private SplitTimeListId splitTimeListId;

    public static PersonRaceResult of(String classResultShortName,
                                      Long personId,
                                      ZonedDateTime startTime,
                                      ZonedDateTime finishTime,
                                      Double punchTime,
                                      Long position,
                                      @NonNull Byte raceNumber,
                                      @NonNull ResultStatus resultState) {
        return PersonRaceResult.of(classResultShortName,
            personId,
            startTime,
            finishTime,
            punchTime,
            position,
            resultState,
            raceNumber,
            null);
    }

    public static PersonRaceResult of(String classResultShortName,
                                      Long personId,
                                      ZonedDateTime startTime,
                                      ZonedDateTime finishTime,
                                      Double punchTime,
                                      Long position,
                                      @NonNull ResultStatus resultState,
                                      @NonNull Byte raceNumber,
                                      @Nullable SplitTimeListId splitTimeListId) {
        return new PersonRaceResult(ClassResultShortName.of(classResultShortName),
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
    public int compareTo(@NonNull PersonRaceResult o) {
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
