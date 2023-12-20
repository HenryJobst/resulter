package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ShallowLoadProxy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
public class PersonRaceResult {
    @NonNull
    @Setter
    private PersonRaceResultId id;
    @NonNull
    private final PersonResultId personResultId;
    @NonNull
    private final RaceNumber raceNumber;
    @NonNull
    private final DateTime startTime;
    @NonNull
    private final DateTime finishTime;
    @NonNull
    private final PunchTime runtime;
    @NonNull
    private final Position positon;
    @NonNull
    private final ResultStatus state;
    @NonNull
    ShallowLoadProxy<SplitTimes> splitTimes;

    public PersonRaceResult(@NonNull PersonRaceResultId id,
                            @NonNull PersonResultId personResultId,
                            @NonNull RaceNumber raceNumber,
                            @NonNull DateTime startTime,
                            @NonNull DateTime finishTime,
                            @NonNull PunchTime runtime,
                            @NonNull Position positon,
                            @NonNull ResultStatus state,
                            @NonNull ShallowLoadProxy<SplitTimes> splitTimes) {
        this.id = id;
        this.personResultId = personResultId;
        this.raceNumber = raceNumber;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.runtime = runtime;
        this.positon = positon;
        this.state = state;
        this.splitTimes = splitTimes;
    }

    public static PersonRaceResult of(Long raceNumber,
                                      ZonedDateTime startTime,
                                      ZonedDateTime finishTime,
                                      Double punchTime,
                                      Long position,
                                      @NonNull ResultStatus resultState,
                                      @Nullable List<SplitTime> splitTimes) {
        return PersonRaceResult.of(
                PersonRaceResultId.empty().value(),
                PersonResultId.empty().value(),
                raceNumber,
                startTime,
                finishTime,
                punchTime,
                position,
                resultState,
                splitTimes);
    }

    public static PersonRaceResult of(
            @NonNull Long id,
            @NonNull Long personResultId,
            Long raceNumber,
            ZonedDateTime startTime,
            ZonedDateTime finishTime,
            Double punchTime,
            Long position,
            ResultStatus resultState,
            List<SplitTime> splitTimes) {
        return new PersonRaceResult(
                PersonRaceResultId.of(id),
                PersonResultId.of(personResultId),
                RaceNumber.of(raceNumber),
                DateTime.of(startTime),
                DateTime.of(finishTime),
                PunchTime.of(punchTime),
                Position.of(position),
                resultState,
                splitTimes != null ? ShallowLoadProxy.of(SplitTimes.of(splitTimes)) : ShallowLoadProxy.empty());
    }
}
