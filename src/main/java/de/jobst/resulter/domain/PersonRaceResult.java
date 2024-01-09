package de.jobst.resulter.domain;

import de.jobst.resulter.domain.util.ShallowLoadProxy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;

@Getter
public class PersonRaceResult implements Comparable<PersonRaceResult> {
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
    private final Position position;
    @NonNull
    private final ResultStatus state;
    @NonNull
    ShallowLoadProxy<SplitTimes> splitTimes;
    @NonNull
    @Setter
    private CupScores cupScores;

    public PersonRaceResult(@NonNull PersonRaceResultId id,
                            @NonNull PersonResultId personResultId,
                            @NonNull RaceNumber raceNumber,
                            @NonNull DateTime startTime,
                            @NonNull DateTime finishTime,
                            @NonNull PunchTime runtime,
                            @NonNull Position position,
                            @NonNull ResultStatus state,
                            @NonNull ShallowLoadProxy<SplitTimes> splitTimes,
                            @NonNull CupScores cupScores) {
        this.id = id;
        this.personResultId = personResultId;
        this.raceNumber = raceNumber;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.runtime = runtime;
        this.position = position;
        this.state = state;
        this.splitTimes = splitTimes;
        this.cupScores = cupScores;
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
                splitTimes != null ? ShallowLoadProxy.of(SplitTimes.of(splitTimes)) : ShallowLoadProxy.empty(),
                CupScores.of(new HashMap<>()));
    }

    @Override
    public int compareTo(@NonNull PersonRaceResult o) {
        int val = this.raceNumber.compareTo(o.raceNumber);
        if (val == 0) {
            val = this.position.compareTo(o.position);
        }
        if (val == 0) {
            val = this.id.compareTo(o.id);
        }
        return val;
    }

    public void setScore(CupType type, CupScore score) {
        cupScores.add(type, score);
    }
}
