package de.jobst.resulter.domain;

import java.time.LocalDateTime;
import java.util.List;

public record PersonRaceResult(RaceNumber raceNumber, DateTime startTime, DateTime finishTime, PunchTime runtime,
                               Position positon, ResultStatus state, SplitTimes splitTimes) {
    public static PersonRaceResult of(Long raceNumber,
                                      LocalDateTime startTime,
                                      LocalDateTime finishTime,
                                      Double punchTime,
                                      Long position,
                                      String resultState,
                                      List<SplitTime> splitTimes) {
        return new PersonRaceResult(RaceNumber.of(raceNumber),
                DateTime.of(startTime),
                DateTime.of(finishTime),
                PunchTime.of(punchTime),
                Position.of(position),
                ResultStatus.fromValue(resultState),
                SplitTimes.of(splitTimes));
    }
}
