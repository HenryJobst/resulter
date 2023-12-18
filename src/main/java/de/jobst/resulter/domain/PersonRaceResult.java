package de.jobst.resulter.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record PersonRaceResult(PersonRaceResultId id,
                               RaceNumber raceNumber,
                               DateTime startTime,
                               DateTime finishTime,
                               PunchTime runtime,
                               Position positon,
                               ResultStatus state,
                               Optional<SplitTimes> splitTimes) {
    public static PersonRaceResult of(Long raceNumber,
                                      LocalDateTime startTime,
                                      LocalDateTime finishTime,
                                      Double punchTime,
                                      Long position,
                                      ResultStatus resultState,
                                      @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                      Optional<List<SplitTime>> splitTimes) {
        return new PersonRaceResult(
                PersonRaceResultId.of(0L),
                RaceNumber.of(raceNumber),
                DateTime.of(startTime),
                DateTime.of(finishTime),
                PunchTime.of(punchTime),
                Position.of(position),
                resultState,
                splitTimes.map(SplitTimes::of));
    }
}
