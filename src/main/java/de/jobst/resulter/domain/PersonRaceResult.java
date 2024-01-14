package de.jobst.resulter.domain;

import org.springframework.lang.NonNull;

import java.time.ZonedDateTime;

public record PersonRaceResult(@NonNull RaceNumber raceNumber, @NonNull DateTime startTime,
                               @NonNull DateTime finishTime, @NonNull PunchTime runtime, @NonNull Position position,
                               @NonNull ResultStatus state) implements Comparable<PersonRaceResult> {

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
            resultState);
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
