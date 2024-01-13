package de.jobst.resulter.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.time.ZonedDateTime;

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

    public PersonRaceResult(@NonNull PersonRaceResultId id,
                            @NonNull PersonResultId personResultId,
                            @NonNull RaceNumber raceNumber,
                            @NonNull DateTime startTime,
                            @NonNull DateTime finishTime,
                            @NonNull PunchTime runtime,
                            @NonNull Position position,
                            @NonNull ResultStatus state) {
        this.id = id;
        this.personResultId = personResultId;
        this.raceNumber = raceNumber;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.runtime = runtime;
        this.position = position;
        this.state = state;
    }

    public static PersonRaceResult of(Long raceNumber,
                                      ZonedDateTime startTime,
                                      ZonedDateTime finishTime,
                                      Double punchTime,
                                      Long position,
                                      @NonNull ResultStatus resultState) {
        return PersonRaceResult.of(PersonRaceResultId.empty().value(),
            PersonResultId.empty().value(),
            raceNumber,
            startTime,
            finishTime,
            punchTime,
            position,
            resultState);
    }

    public static PersonRaceResult of(@NonNull Long id,
                                      @NonNull Long personResultId,
                                      Long raceNumber,
                                      ZonedDateTime startTime,
                                      ZonedDateTime finishTime,
                                      Double punchTime,
                                      Long position,
                                      ResultStatus resultState) {
        return new PersonRaceResult(PersonRaceResultId.of(id),
            PersonResultId.of(personResultId),
            RaceNumber.of(raceNumber),
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
        if (val == 0) {
            val = this.id.compareTo(o.id);
        }
        return val;
    }
}
