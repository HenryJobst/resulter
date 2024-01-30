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
    private PersonId personId;
    @NonNull
    private RaceNumber raceNumber;
    @NonNull
    DateTime startTime;
    @NonNull
    DateTime finishTime;
    @NonNull
    PunchTime runtime;
    @NonNull
    Position position;
    @NonNull
    ResultStatus state;
    @Nullable
    @Setter
    SplitTimeListId splitTimeListId;

    public static PersonRaceResult of(String classResultShortName,
                                      Long personId,
                                      Long raceNumber,
                                      ZonedDateTime startTime,
                                      ZonedDateTime finishTime,
                                      Double punchTime,
                                      Long position,
                                      @NonNull ResultStatus resultState) {
        return PersonRaceResult.of(classResultShortName,
            personId,
            raceNumber,
            startTime,
            finishTime,
            punchTime,
            position,
            resultState,
            null);
    }

    public static PersonRaceResult of(String classResultShortName,
                                      Long personId,
                                      Long raceNumber,
                                      ZonedDateTime startTime,
                                      ZonedDateTime finishTime,
                                      Double punchTime,
                                      Long position,
                                      @NonNull ResultStatus resultState,
                                      @Nullable SplitTimeListId splitTimeListId) {
        return new PersonRaceResult(ClassResultShortName.of(classResultShortName),
            PersonId.of(personId),
            RaceNumber.of(raceNumber),
            DateTime.of(startTime),
            DateTime.of(finishTime),
            PunchTime.of(punchTime),
            Position.of(position),
            resultState,
            splitTimeListId);
    }


    @Override
    public int compareTo(@NonNull PersonRaceResult o) {
        int val = this.position.compareTo(o.position);
        if (val == 0) {
            val = this.raceNumber.compareTo(o.raceNumber);
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
