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
    RaceNumber raceNumber;
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
    @Setter
    @Nullable
    SplitTimeListId splitTimeListId;

    public static PersonRaceResult of(Long raceNumber,
                                      ZonedDateTime startTime,
                                      ZonedDateTime finishTime,
                                      Double punchTime,
                                      Long position,
                                      @NonNull ResultStatus resultState) {
        return new PersonRaceResult(RaceNumber.of(raceNumber),
            DateTime.of(startTime),
            DateTime.of(finishTime),
            PunchTime.of(punchTime),
            Position.of(position),
            resultState,
            null);
    }

    @Override
    public int compareTo(@NonNull PersonRaceResult o) {
        int val = this.raceNumber.compareTo(o.raceNumber);
        if (val == 0) {
            val = this.position.compareTo(o.position);
        }
        return val;
    }
}
